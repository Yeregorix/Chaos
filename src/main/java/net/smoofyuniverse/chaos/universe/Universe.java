/*
 * Copyright (c) 2019 Hugo Dupanloup (Yeregorix)
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.function.IntConsumer;

public final class Universe {
	public final List<Particle> particles = new ArrayList<>();
	private double sizeX, sizeY, halfX, halfY;
	private Executor executor;
	private int chunks;

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

	public void tick() {
		if (this.sizeX == 0 || this.sizeY == 0)
			throw new IllegalStateException("Invalid size");

		int size = this.particles.size();
		forEach(i -> {
			Particle p = this.particles.get(i);
			if (p.type == null)
				return;

			for (int j = 0; j < size; j++) {
				if (i == j)
					continue;

				Particle p2 = this.particles.get(j);
				if (p2.type == null)
					continue;

				p2.type.applyInteractions(p2, p);
				if (p.type == null)
					break;
			}
		}, size);

		forEach(i -> {
			Particle p = this.particles.get(i);
			if (p.type == null)
				return;

			p.type.tickStandalone(p);
			if (p.type == null)
				return;

			validatePositionX(p);
			validatePositionY(p);

			p.accelerationX = 0;
			p.accelerationY = 0;
		}, size);

		this.particles.removeIf(p -> p.type == null);
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
			throw new RuntimeException(e);
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
			array[i] = this.particles.get(i).copy();

		return new Snapshot(this.sizeX, this.sizeY, array);
	}

	private interface BiDoubleConsumer {
		void accept(double a, double b);
	}
}
