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

package net.smoofyuniverse.chaos.impl;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import net.smoofyuniverse.chaos.type.ColoredType;
import net.smoofyuniverse.chaos.universe.Particle;
import net.smoofyuniverse.chaos.universe.Universe;

import java.util.Random;

public class TypeA implements ColoredType {
	public final Color color;
	public final double radius, friction;
	public final double attractionFactor, attractionRadius, repulsionFactor, repulsionRadius;
	public final boolean flatForce;

	private final double attractionRadius2;
	private final Color attractionColor, repulsionColor;

	public TypeA(Color color, double radius, double friction, double attractionFactor, double attractionRadius, double repulsionFactor, double repulsionRadius, boolean flatForce) {
		if (color == null)
			throw new IllegalArgumentException("color");
		if (radius <= 0)
			throw new IllegalArgumentException("radius");
		if (friction < 0 || friction > 1)
			throw new IllegalArgumentException("friction");
		if (attractionRadius < 0)
			throw new IllegalArgumentException("attractionRadius");
		if (repulsionRadius < 0)
			throw new IllegalArgumentException("repulsionRadius");
		if (repulsionRadius > attractionRadius)
			throw new IllegalArgumentException("repulsionRadius, attractionRadius");

		this.color = color;
		this.radius = radius;
		this.friction = friction;
		this.attractionFactor = attractionFactor;
		this.attractionRadius = attractionRadius;
		this.repulsionFactor = repulsionFactor;
		this.repulsionRadius = repulsionRadius;
		this.flatForce = flatForce;

		this.repulsionColor = Color.color(color.getRed(), color.getGreen(), color.getBlue(), 0.2);
		this.attractionColor = Color.color(color.getRed(), color.getGreen(), color.getBlue(), 0.05);
		this.attractionRadius2 = attractionRadius * attractionRadius;
	}

	@Override
	public Color getColor() {
		return this.color;
	}

	@Override
	public void draw1(GraphicsContext g, Particle p) {
		g.setStroke(this.repulsionColor);
		p.universe.strokeCircle(g, p.positionX, p.positionY, this.repulsionRadius);
		g.setStroke(this.attractionColor);
		p.universe.strokeCircle(g, p.positionX, p.positionY, this.attractionRadius);
	}

	@Override
	public void draw2(GraphicsContext g, Particle p) {
		g.setFill(this.color);
		p.universe.fillCircle(g, p.positionX, p.positionY, this.radius);
	}

	@Override
	public Particle createDefault(Universe universe) {
		Particle p = new Particle(universe);
		p.type = this;
		p.radius = this.radius;
		return p;
	}

	@Override
	public Particle createRandom(Universe universe, Random random) {
		Particle p = createDefault(universe);
		p.positionX = random.nextDouble() * universe.getSizeX();
		p.positionY = random.nextDouble() * universe.getSizeY();
		p.speedX = random.nextGaussian() * 0.2d;
		p.speedY = random.nextGaussian() * 0.2d;
		return p;
	}

	@Override
	public void applyInteractions(Particle emitter, Particle receiver) {
		double dx = receiver.universe.getDeltaX(receiver.positionX, emitter.positionX);
		double dy = receiver.universe.getDeltaY(receiver.positionY, emitter.positionY);
		double d2 = dx * dx + dy * dy;

		if (d2 > this.attractionRadius2 || d2 < 0.01f)
			return;

		double d = Math.sqrt(d2);
		dx /= d;
		dy /= d;

		double f;
		if (d > this.repulsionRadius) {
			if (this.flatForce) {
				f = this.attractionFactor;
			} else {
				double numer = 2d * Math.abs(d - 0.5d * (this.attractionRadius + this.repulsionRadius));
				double denom = this.attractionRadius - this.repulsionRadius;
				f = this.attractionFactor * (1d - numer / denom);
			}
		} else {
			f = this.repulsionFactor * this.repulsionRadius * (1d / (this.repulsionRadius + 2) - 1d / (d + 2));
		}

		receiver.accelerationX += dx * f;
		receiver.accelerationY += dy * f;
	}

	@Override
	public void tickStandalone(Particle p) {
		if (p.selected) {
			p.speedX = 0;
			p.speedY = 0;
		} else {
			p.speedX = p.speedX * (1 - this.friction) + p.accelerationX;
			p.speedY = p.speedY * (1 - this.friction) + p.accelerationY;

			p.positionX += p.speedX;
			p.positionY += p.speedY;
		}
	}
}
