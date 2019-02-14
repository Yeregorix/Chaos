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

import javafx.scene.paint.Color;
import net.smoofyuniverse.chaos.impl.TypeA;
import net.smoofyuniverse.chaos.impl.TypeABuilder;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class TypeAGenerators {
	public static final TypeARandomGenerator BALANCED = new TypeARandomGenerator("Balanced", 9, -0.02f, 0.06f, 0.0f, 20.0f, 20.0f, 70.0f, 0.05f, false),
			CHAOS = new TypeARandomGenerator("Chaos", 6, 0.02f, 0.04f, 0.0f, 30.0f, 30.0f, 100.0f, 0.01f, false),
			DIVERSITY = new TypeARandomGenerator("Diversity", 12, -0.01f, 0.04f, 0.0f, 20.0f, 10.0f, 60.0f, 0.05f, true),
			FRICTIONLESS = new TypeARandomGenerator("Frictionless", 6, 0.01f, 0.005f, 10.0f, 10.0f, 10.0f, 60.0f, 0.0f, true),
			GLIDERS = new TypeARandomGenerator("Gliders", 6, 0.0f, 0.06f, 0.0f, 20.0f, 10.0f, 50.0f, 0.1f, true),
			HOMOGENEITY = new TypeARandomGenerator("Homogeneity", 4, 0.0f, 0.04f, 10.0f, 10.0f, 10.0f, 80.0f, 0.05f, true),
			LARGE_CLUSTERS = new TypeARandomGenerator("Large clusters", 6, 0.025f, 0.02f, 0.0f, 30.0f, 30.0f, 100.0f, 0.2f, false),
			MEDIUM_CLUSTERS = new TypeARandomGenerator("Medium clusters", 6, 0.02f, 0.05f, 0.0f, 20.0f, 20.0f, 50.0f, 0.05f, false),
			QUIESCENCE = new TypeARandomGenerator("Quiescence", 6, -0.02f, 0.1f, 10.0f, 20.0f, 20.0f, 60.0f, 0.2f, false),
			SMALL_CLUSTERS = new TypeARandomGenerator("Small clusters", 6, -0.005f, 0.01f, 10.0f, 10.0f, 20.0f, 50.0f, 0.01f, false);

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

	private static final TypeA WANDERER_TYPE = new TypeA(Color.WHITE, 8, 0.005, 1.4, 43, 10, 40, false);

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
			return new TypeABuilder(WANDERER_TYPE);
		}
	};
}
