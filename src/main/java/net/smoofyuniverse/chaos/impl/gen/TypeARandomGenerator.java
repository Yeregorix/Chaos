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

package net.smoofyuniverse.chaos.impl.gen;

import net.smoofyuniverse.chaos.impl.TypeABuilder;
import net.smoofyuniverse.chaos.util.NumberGenerator;

import java.util.Random;

public final class TypeARandomGenerator implements TypeAGenerator {
	public final String name;
	public final int recommendedTypes;
	public final NumberGenerator radius, attractionFactor, attractionRadius, repulsionFactor, repulsionRadius, friction;
	public final double flatAttractionChance;

	public TypeARandomGenerator(String name, int recommendedTypes, NumberGenerator radius, NumberGenerator attractionFactor, NumberGenerator attractionRadius, NumberGenerator repulsionFactor, NumberGenerator repulsionRadius, NumberGenerator friction, double flatAttractionChance) {
		this.name = name;
		this.recommendedTypes = recommendedTypes;
		this.radius = radius;
		this.attractionFactor = attractionFactor;
		this.attractionRadius = attractionRadius;
		this.repulsionFactor = repulsionFactor;
		this.repulsionRadius = repulsionRadius;
		this.friction = friction;
		this.flatAttractionChance = flatAttractionChance;
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
		double radius = this.radius.generate(random);
		if (radius < 0)
			radius = 0;

		double repulsionRadius = this.repulsionRadius.generate(random);
		if (repulsionRadius < radius)
			repulsionRadius = radius;

		double attractionRadius = this.attractionRadius.generate(random);
		if (attractionRadius < repulsionRadius)
			attractionRadius = repulsionRadius;

		double friction = this.friction.generate(random);
		if (friction < 0)
			friction = 0;
		else if (friction > 1)
			friction = 1;

		TypeABuilder b = new TypeABuilder();
		b.radius.setValue(radius);
		b.friction.setValue(friction);
		b.attractionFactor.setValue(this.attractionFactor.generate(random));
		b.attractionRadius.setValue(attractionRadius);
		b.repulsionFactor.setValue(this.repulsionFactor.generate(random));
		b.repulsionRadius.setValue(repulsionRadius);
		b.flatAttraction.setSelected(random.nextDouble() < this.flatAttractionChance);
		return b;
	}
}
