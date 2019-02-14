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

package net.smoofyuniverse.chaos.impl.gen;

import net.smoofyuniverse.chaos.impl.TypeABuilder;

import java.util.Random;

public final class TypeARandomGenerator implements TypeAGenerator {
	public final String name;
	public final int recommendedTypes;
	public final double attractionMean, attractionStd,
			repulsionRadiusMin, repulsionRadiusMax,
			attractionRadiusMin, attractionRadiusMax,
			friction;
	public final boolean flatForce;

	public TypeARandomGenerator(String name, int recommendedTypes, double attractionMean, double attractionStd, double repulsionRadiusMin, double repulsionRadiusMax, double attractionRadiusMin, double attractionRadiusMax, double friction, boolean flatForce) {
		this.name = name;
		this.recommendedTypes = recommendedTypes;
		this.attractionMean = attractionMean;
		this.attractionStd = attractionStd;
		this.repulsionRadiusMin = repulsionRadiusMin;
		this.repulsionRadiusMax = repulsionRadiusMax;
		this.attractionRadiusMin = attractionRadiusMin;
		this.attractionRadiusMax = attractionRadiusMax;
		this.friction = friction;
		this.flatForce = flatForce;
	}

	@Override
	public int recommendedTypes() {
		return this.recommendedTypes;
	}

	@Override
	public String getDisplayName() {
		return "A (" + this.name + ") [" + this.recommendedTypes + "]";
	}

	@Override
	public TypeABuilder generate(Random random) {
		double repulsionRadius = this.repulsionRadiusMin + random.nextDouble() * (this.repulsionRadiusMax - this.repulsionRadiusMin);
		double attractionRadius = Math.max(this.attractionRadiusMin + random.nextDouble() * (this.attractionRadiusMax - this.attractionRadiusMin), repulsionRadius);

		TypeABuilder b = new TypeABuilder();
		b.radius.setValue(5);
		b.friction.setValue(this.friction);
		b.attractionFactor.setValue(random.nextGaussian() * this.attractionStd + this.attractionMean);
		b.attractionRadius.setValue(attractionRadius);
		b.repulsionFactor.setValue(2);
		b.repulsionRadius.setValue(repulsionRadius);
		b.flatForce.setSelected(this.flatForce);
		return b;
	}
}
