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
import javafx.scene.paint.Color;

public final class Snapshot {
	public final double sizeX, sizeY;
	public final Particle[] particles;

	public Snapshot(double sizeX, double sizeY, Particle[] particles) {
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		this.particles = particles;
	}

	public void render(GraphicsContext g) {
		g.setFill(Color.BLACK);
		g.fillRect(0, 0, this.sizeX, this.sizeY);

		for (Particle p : particles)
			p.type.draw1(g, p);
		for (Particle p : particles)
			p.type.draw2(g, p);
	}
}
