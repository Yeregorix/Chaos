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
import net.smoofyuniverse.chaos.universe.IParticle;
import net.smoofyuniverse.chaos.universe.Particle;
import net.smoofyuniverse.chaos.universe.Universe;

import java.util.Random;

public class TypeA implements ColoredType {
	public final Color color;
	public final double radius, friction;
	public final double attractionFactor, attractionRadius, repulsionFactor, repulsionRadius;
	public final boolean flatAttraction;
	public final Universe universe;

	// Cached values
	private final double attractionRadius2, mRadius, dRadius;
	private final Color attractionColor, repulsionColor;

	public TypeA(Universe universe, Color color, double radius, double friction, double attractionFactor, double attractionRadius, double repulsionFactor, double repulsionRadius, boolean flatAttraction) {
		if (universe == null)
			throw new IllegalArgumentException("universe");
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
		if (radius > repulsionRadius)
			throw new IllegalArgumentException("radius, repulsionRadius");
		if (repulsionRadius > attractionRadius)
			throw new IllegalArgumentException("repulsionRadius, attractionRadius");

		this.universe = universe;
		this.color = color;
		this.radius = radius;
		this.friction = friction;
		this.attractionFactor = attractionFactor;
		this.attractionRadius = attractionRadius;
		this.repulsionFactor = repulsionFactor;
		this.repulsionRadius = repulsionRadius;
		this.flatAttraction = flatAttraction;

		this.repulsionColor = Color.color(color.getRed(), color.getGreen(), color.getBlue(), 0.2);
		this.attractionColor = Color.color(color.getRed(), color.getGreen(), color.getBlue(), 0.05);
		this.attractionRadius2 = attractionRadius * attractionRadius;
		this.mRadius = (attractionRadius + repulsionRadius) / 2D;
		this.dRadius = attractionRadius - repulsionRadius;
	}

	@Override
	public Color getColor() {
		return this.color;
	}

	@Override
	public void draw1(GraphicsContext g, IParticle p) {
		g.setStroke(this.repulsionColor);
		this.universe.strokeCircle(g, p.getPositionX(), p.getPositionY(), this.repulsionRadius);
		g.setStroke(this.attractionColor);
		this.universe.strokeCircle(g, p.getPositionX(), p.getPositionY(), this.attractionRadius);
	}

	@Override
	public void draw2(GraphicsContext g, IParticle p) {
		g.setFill(this.color);
		this.universe.fillCircle(g, p.getPositionX(), p.getPositionY(), this.radius);
	}

	@Override
	public Particle createDefault() {
		Particle p = new Particle();
		p.type = this;
		p.radius = this.radius;
		return p;
	}

	@Override
	public Particle createRandom(Random random) {
		Particle p = createDefault();
		p.positionX = random.nextDouble() * universe.getSizeX();
		p.positionY = random.nextDouble() * universe.getSizeY();
		p.speedX = random.nextGaussian() * 0.2D;
		p.speedY = random.nextGaussian() * 0.2D;
		return p;
	}

	@Override
	public void applyInteractions(IParticle emitter, Particle receiver) {
		double dx = this.universe.getDeltaX(receiver.positionX, emitter.getPositionX());
		double dy = this.universe.getDeltaY(receiver.positionY, emitter.getPositionY());
		double d2 = dx * dx + dy * dy;

		if (d2 > this.attractionRadius2 || d2 < 0.01D)
			return;

		double d = Math.sqrt(d2);
		dx /= d;
		dy /= d;

		double f;
		if (d > this.repulsionRadius) {
			if (this.flatAttraction)
				f = this.attractionFactor;
			else
				f = this.attractionFactor * (1D - (2D * Math.abs(d - this.mRadius)) / this.dRadius);
		} else {
			f = this.repulsionFactor * this.repulsionRadius * (1D / (this.repulsionRadius + 2) - 1D / (d + 2));
		}

		receiver.accelerationX += dx * f;
		receiver.accelerationY += dy * f;
	}

	@Override
	public void tickStandalone(Particle p, boolean selected) {
		if (selected) {
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
