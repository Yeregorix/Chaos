/*
 * Copyright (c) 2019-2021 Hugo Dupanloup (Yeregorix)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.smoofyuniverse.chaos.universe;

import javafx.scene.canvas.GraphicsContext;
import net.smoofyuniverse.chaos.type.Type;
import net.smoofyuniverse.common.logger.ApplicationLogger;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.function.IntConsumer;

public final class Universe {
	private static final Logger logger = ApplicationLogger.get(Universe.class);

	private final List<UParticle> particles = new ArrayList<>();
	private final Executor executor;
	private final int chunks;

	private double sizeX, sizeY, halfX, halfY;
	private UParticle selection;

	public Universe(Executor executor, int chunks) {
		this.executor = executor;
		this.chunks = chunks;
	}

	public double getSizeX() {
		return this.sizeX;
	}

	public void setSizeX(double value) {
		if (value <= 0)
			throw new IllegalArgumentException();
		this.sizeX = value;
		this.halfX = value / 2d;
	}

	public double getSizeY() {
		return this.sizeY;
	}

	public void setSizeY(double value) {
		if (value <= 0)
			throw new IllegalArgumentException();
		this.sizeY = value;
		this.halfY = value / 2d;
	}

	public double getDeltaX(double x1, double x2) {
		double d = x2 - x1;
		if (d > this.halfX)
			return d - this.sizeX;
		if (d < -this.halfX)
			return d + this.sizeX;
		return d;
	}

	public double getDeltaY(double y1, double y2) {
		double d = y2 - y1;
		if (d > this.halfY)
			return d - this.sizeY;
		if (d < -this.halfY)
			return d + this.sizeY;
		return d;
	}

	public void strokeCircle(GraphicsContext g, double x, double y, double radius) {
		double radius2 = radius * 2;
		doAt(x, y, radius, (a, b) -> g.strokeOval(a - radius, b - radius, radius2, radius2));
	}

	private void doAt(double x, double y, double radius, BiDoubleConsumer consumer) {
		consumer.accept(x, y);

		double dx = 0, dy = 0;

		if (x < radius)
			dx = this.sizeX;
		else if (this.sizeX - x < radius)
			dx = -this.sizeX;

		if (y < radius)
			dy = this.sizeY;
		else if (this.sizeY - y < radius)
			dy = -this.sizeY;

		if (dx == 0) {
			if (dy != 0)
				consumer.accept(x, y + dy); // just y
		} else {
			consumer.accept(x + dx, y); // just x
			if (dy != 0) {
				consumer.accept(x, y + dy); // just y
				consumer.accept(x + dx, y + dy); // both
			}
		}
	}

	public void fillCircle(GraphicsContext g, double x, double y, double radius) {
		double radius2 = radius * 2;
		doAt(x, y, radius, (a, b) -> g.fillOval(a - radius, b - radius, radius2, radius2));
	}

	public void add(Particle particle) {
		if (particle.type != null)
			this.particles.add(new UParticle(particle));
	}

	public void clear() {
		this.particles.clear();
		this.selection = null;
	}

	public void select(double x, double y) {
		double sel_d2 = Double.MAX_VALUE;
		UParticle sel = null;

		for (UParticle p : this.particles) {
			double dx = p.positionX - x, dy = p.positionY - y;
			double d2 = dx * dx + dy * dy;
			if (d2 < sel_d2 && d2 < p.radius * p.radius) {
				sel_d2 = d2;
				sel = p;
			}
		}

		if (sel != null)
			select(sel);
	}

	public void select(UParticle p) {
		if (this.selection != null)
			this.selection.selected = false;

		p.selected = true;
		this.selection = p;
	}

	public void deselect() {
		if (this.selection != null)
			this.selection.selected = false;
		this.selection = null;
	}

	public void moveSelection(double x, double y) {
		if (this.selection == null || this.selection.type == null)
			return;

		this.selection.mutable.positionX = x;
		this.selection.mutable.positionY = y;
		this.selection.positionX = x;
		this.selection.positionY = y;
	}

	public void tick() {
		if (this.sizeX == 0 || this.sizeY == 0)
			throw new IllegalStateException("Invalid size");

		int size = this.particles.size();
		forEach(i -> {
			UParticle r = this.particles.get(i);

			for (int j = 0; j < size; j++) {
				if (i != j) {
					UParticle e = this.particles.get(j);
					e.type.applyInteractions(e, r);
				}
			}
		}, size);

		forEach(i -> {
			UParticle p = this.particles.get(i);
			p.type.tickStandalone(p);

			if (p.mutable.type == null)
				return;

			validatePositionX(p.mutable);
			validatePositionY(p.mutable);

			p.mutable.forceX = 0;
			p.mutable.forceY = 0;
		}, size);

		this.particles.removeIf(p -> {
			if (p.mutable.type == null)
				return true;
			p.save();
			return false;
		});
	}

	private void forEach(IntConsumer consumer, int size) {
		CountDownLatch latch = new CountDownLatch(this.chunks);

		int chunkSize = size / this.chunks;
		int i = 0;
		while (i < this.chunks) {
			int start = i * chunkSize;
			i++;
			int end = i == this.chunks ? size : i * chunkSize;

			this.executor.execute(() -> {
				for (int j = start; j < end; j++)
					consumer.accept(j);
				latch.countDown();
			});
		}

		try {
			latch.await();
		} catch (InterruptedException e) {
			logger.error("Interruption", e);
		}
	}

	private void validatePositionX(Particle particle) {
		double dx = particle.positionX;
		if (dx < 0) {
			particle.positionX += Math.ceil(-dx / this.sizeX) * this.sizeX;
		} else {
			dx -= this.sizeX;
			if (dx > 0)
				particle.positionX -= Math.ceil(dx / this.sizeX) * this.sizeX;
		}
	}

	private void validatePositionY(Particle particle) {
		double dy = particle.positionY;
		if (dy < 0) {
			particle.positionY += Math.ceil(-dy / this.sizeY) * this.sizeY;
		} else {
			dy -= this.sizeY;
			if (dy > 0)
				particle.positionY -= Math.ceil(dy / this.sizeY) * this.sizeY;
		}
	}

	public Snapshot snapshot() {
		Particle[] array = new Particle[this.particles.size()];

		for (int i = 0; i < array.length; i++)
			array[i] = this.particles.get(i).mutable.copy();

		return new Snapshot(this.sizeX, this.sizeY, array);
	}

	private interface BiDoubleConsumer {
		void accept(double a, double b);
	}

	public static class UParticle implements IParticle {
		public final Particle mutable;
		private double accelerationX, accelerationY;
		private double speedX, speedY;
		private double positionX, positionY;
		private double radius;
		private long ticks;
		private Type type;
		private boolean selected;

		private UParticle(Particle mutable) {
			this.mutable = mutable;
			save();
		}

		private void save() {
			this.accelerationX = this.mutable.accelerationX;
			this.accelerationY = this.mutable.accelerationY;
			this.speedX = this.mutable.speedX;
			this.speedY = this.mutable.speedY;
			this.positionX = this.mutable.positionX;
			this.positionY = this.mutable.positionY;
			this.radius = this.mutable.radius;
			this.ticks = this.mutable.ticks;
			this.type = this.mutable.type;
		}

		@Override
		public double getAccelerationX() {
			return this.accelerationX;
		}

		@Override
		public double getAccelerationY() {
			return this.accelerationY;
		}

		@Override
		public double getSpeedX() {
			return this.speedX;
		}

		@Override
		public double getSpeedY() {
			return this.speedY;
		}

		@Override
		public double getPositionX() {
			return this.positionX;
		}

		@Override
		public double getPositionY() {
			return this.positionY;
		}

		@Override
		public double getRadius() {
			return this.radius;
		}

		@Override
		public long getTicks() {
			return this.ticks;
		}

		@Override
		public Type getType() {
			return this.type;
		}

		public boolean isSelected() {
			return this.selected;
		}
	}
}
