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

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import net.smoofyuniverse.common.app.ApplicationManager;
import net.smoofyuniverse.common.logger.ApplicationLogger;
import org.slf4j.Logger;

public abstract class CachedImageGenerator implements BackgroundGenerator {
	private static final Logger logger = ApplicationLogger.get(CachedImageGenerator.class);

	private final ImageView imageView = new ImageView();

	private Image cachedImage;
	private int cachedSizeX, cachedSizeY;

	private volatile boolean updating;
	private int newSizeX, newSizeY;

	@Override
	public void resize(double sizeX, double sizeY) {
		int x = (int) Math.ceil(sizeX), y = (int) Math.ceil(sizeY);

		if (x > this.cachedSizeX && x > this.newSizeX)
			this.newSizeX = x;
		if (y > this.cachedSizeY && y > this.newSizeY)
			this.newSizeY = y;

		if (this.newSizeX != 0 || this.newSizeY != 0) {
			if (this.newSizeX == 0)
				this.newSizeX = this.cachedSizeX;
			if (this.newSizeY == 0)
				this.newSizeY = this.cachedSizeY;

			if (!this.updating) {
				this.updating = true;
				ApplicationManager.get().getExecutor().execute(() -> {
					try {
						update();
					} catch (Exception e) {
						logger.error("Failed to update image", e);
					}
				});
			}
		}
	}

	private void update() {
		while (this.newSizeX != 0 && this.newSizeY != 0) {
			int sizeX = this.newSizeX, sizeY = this.newSizeY;
			this.newSizeX = this.newSizeY = 0;

			WritableImage newImage = new WritableImage(sizeX, sizeY);
			if (this.cachedImage != null)
				newImage.getPixelWriter().setPixels(0, 0, this.cachedSizeX, this.cachedSizeY, this.cachedImage.getPixelReader(), 0, 0);
			PixelWriter writer = newImage.getPixelWriter();

			for (int x = 0; x < sizeX; x++) {
				for (int y = 0; y < sizeY; y++) {
					if (x >= this.cachedSizeX || y >= this.cachedSizeY)
						writer.setColor(x, y, generate(x, y));
				}
			}

			this.cachedImage = newImage;
			this.cachedSizeX = sizeX;
			this.cachedSizeY = sizeY;
			Platform.runLater(() -> this.imageView.setImage(newImage));
		}

		this.updating = false;
	}

	protected abstract Color generate(int x, int y);

	@Override
	public ImageView getNode() {
		return this.imageView;
	}
}
