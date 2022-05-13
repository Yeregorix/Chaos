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

package net.smoofyuniverse.chaos.background;

import javafx.scene.paint.Color;
import org.spongepowered.noise.NoiseQuality;
import org.spongepowered.noise.module.source.Perlin;

import java.util.Random;
import java.util.logging.Logger;

public class SpaceGenerator extends CachedImageGenerator {
	private static final Logger logger = Logger.getLogger("SpaceGenerator");
	public final Color color1, color2;
	private final Random random = new Random();
	private final Perlin perlin = new Perlin();

	public SpaceGenerator(Color color1, Color color2) {
		this.color1 = color1;
		this.color2 = color2;

		this.perlin.setSeed(this.random.nextInt());
		this.perlin.setOctaveCount(6);
		this.perlin.setFrequency(0.01d);
		this.perlin.setLacunarity(2d);
		this.perlin.setPersistence(0.6d);
		this.perlin.setNoiseQuality(NoiseQuality.STANDARD);
	}

	@Override
	protected Color generate(int x, int y) {
		double v = this.perlin.get(x, y, 0);
		if (this.random.nextFloat() * 1500 < v)
			return Color.gray(this.random.nextDouble());
		return this.color1.interpolate(this.color2, (v - 1.1) * 0.5);
	}
}
