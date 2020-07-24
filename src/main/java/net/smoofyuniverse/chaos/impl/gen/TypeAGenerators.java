/*
 * Copyright (c) 2019-2020 Hugo Dupanloup (Yeregorix)
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

import javafx.scene.paint.Color;
import net.smoofyuniverse.chaos.impl.TypeABuilder;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static net.smoofyuniverse.chaos.util.NumberGenerator.linear;
import static net.smoofyuniverse.chaos.util.NumberGenerator.normal;

public class TypeAGenerators {
	public static final TypeARandomGenerator BALANCED = classicGen("Balanced", 9, -0.02, 0.06, 0.0, 20.0, 20.0, 70.0, 0.05, false),
			CHAOS = classicGen("Chaos", 6, 0.02, 0.04, 0.0, 30.0, 30.0, 100.0, 0.01, false),
			DIVERSITY = classicGen("Diversity", 12, -0.01, 0.04, 0.0, 20.0, 10.0, 60.0, 0.05, true),
			FRICTIONLESS = classicGen("Frictionless", 6, 0.01, 0.005, 10.0, 10.0, 10.0, 60.0, 0.0, true),
			GLIDERS = classicGen("Gliders", 6, 0.0, 0.06, 0.0, 20.0, 10.0, 50.0, 0.1, true),
			HOMOGENEITY = classicGen("Homogeneity", 4, 0.0, 0.04, 10.0, 10.0, 10.0, 80.0, 0.05, true),
			LARGE_CLUSTERS = classicGen("Large clusters", 6, 0.025, 0.02, 0.0, 30.0, 30.0, 100.0, 0.2, false),
			MEDIUM_CLUSTERS = classicGen("Medium clusters", 6, 0.02, 0.05, 0.0, 20.0, 20.0, 50.0, 0.05, false),
			QUIESCENCE = classicGen("Quiescence", 6, -0.02, 0.1, 10.0, 20.0, 20.0, 60.0, 0.2, false),
			SMALL_CLUSTERS = classicGen("Small clusters", 6, -0.005, 0.01, 10.0, 10.0, 20.0, 50.0, 0.01, false);

	public static final List<TypeARandomGenerator> RANDOMS = Arrays.asList(BALANCED, CHAOS, DIVERSITY,
			FRICTIONLESS, GLIDERS, HOMOGENEITY, LARGE_CLUSTERS, MEDIUM_CLUSTERS, QUIESCENCE, SMALL_CLUSTERS);

	public static final TypeAGenerator DEFAULT = new TypeAGenerator() {
		@Override
		public int recommendedTypes() {
			return 1;
		}

		@Override
		public String getDisplayName() {
			return "A (Default)";
		}

		@Override
		public TypeABuilder generate(Random random) {
			return new TypeABuilder();
		}
	};

	public static final TypeAGenerator WANDERER = new TypeAGenerator() {
		@Override
		public int recommendedTypes() {
			return 1;
		}

		@Override
		public String getDisplayName() {
			return "A (Wanderer)";
		}

		@Override
		public TypeABuilder generate(Random random) {
			return new TypeABuilder(Color.WHITE, 8, 0.005, 1.4, 43, 10, 40, 0, 0, false);
		}
	};

	private static TypeARandomGenerator classicGen(String name, int recommendedTypes, double attractionMean, double attractionStd, double repulsionRadiusMin, double repulsionRadiusMax, double attractionRadiusMin, double attractionRadiusMax, double friction, boolean flatAttraction) {
		return new TypeARandomGenerator(name, recommendedTypes, normal(5, 0.5), normal(attractionMean, attractionStd), linear(attractionRadiusMin, attractionRadiusMax), normal(2, 0.5), linear(repulsionRadiusMin, repulsionRadiusMax), normal(friction, 0.001), flatAttraction ? 0.9 : 0.1);
	}
}
