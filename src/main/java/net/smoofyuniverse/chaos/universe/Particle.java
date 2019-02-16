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

import net.smoofyuniverse.chaos.type.Type;

public final class Particle implements IParticle {
	public double accelerationX, accelerationY;
	public double speedX, speedY;
	public double positionX, positionY;
	public double radius;
	public long ticks;
	public Type type;

	public Particle copy() {
		Particle p = new Particle();
		p.accelerationX = this.accelerationX;
		p.accelerationY = this.accelerationY;
		p.speedX = this.speedX;
		p.speedY = this.speedY;
		p.positionX = this.positionX;
		p.positionY = this.positionY;
		p.radius = this.radius;
		p.ticks = this.ticks;
		p.type = this.type;
		return p;
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
	public Type geType() {
		return this.type;
	}
}
