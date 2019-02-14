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

package net.smoofyuniverse.chaos.ui;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import net.smoofyuniverse.chaos.universe.Particle;
import net.smoofyuniverse.chaos.universe.Snapshot;
import net.smoofyuniverse.chaos.universe.Universe;
import net.smoofyuniverse.common.app.App;
import net.smoofyuniverse.common.app.State;
import net.smoofyuniverse.logger.core.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserInterface extends StackPane {
	private static final Logger logger = App.getLogger("UserInterface");

	private final GenerationPanel generationPanel = new GenerationPanel();
	private final Stage stage2 = new Stage();

	private final Canvas canvas = new Canvas();
	private final ExecutorService executor = Executors.newFixedThreadPool(4);
	private final Universe universe = new Universe(this.executor, 12);

	private boolean pause = true, generate = true, details = false;
	private long tau = 25;
	private Particle selection;

	public UserInterface() {
		this.stage2.setScene(new Scene(this.generationPanel));
		this.stage2.setTitle(App.get().getTitle());
		this.stage2.getIcons().addAll(App.get().getStage().get().getIcons());
		this.stage2.setResizable(false);
		this.stage2.setWidth(600);
		this.stage2.setHeight(800);

		this.canvas.widthProperty().bind(widthProperty());
		this.canvas.heightProperty().bind(heightProperty());

		this.canvas.widthProperty().addListener((v, oldV, newV) -> this.universe.setSizeX(newV.doubleValue()));
		this.canvas.heightProperty().addListener((v, oldV, newV) -> this.universe.setSizeY(newV.doubleValue()));

		this.canvas.setOnMousePressed(e -> {
			double x = e.getX(), y = e.getY();
			double sel_d2 = Double.MAX_VALUE;
			Particle sel = null;

			for (Particle p : this.universe.particles) {
				double dx = p.positionX - x, dy = p.positionY - y;
				double d2 = dx * dx + dy * dy;
				if (d2 < sel_d2 && d2 < p.radius * p.radius) {
					sel_d2 = d2;
					sel = p;
				}
			}

			if (sel != null) {
				if (this.selection != null)
					this.selection.selected = false;

				sel.selected = true;
				this.selection = sel;
			}
		});

		this.canvas.setOnMouseReleased(e -> {
			if (this.selection != null)
				this.selection.selected = false;
			this.selection = null;
		});

		this.canvas.setOnMouseDragged(e -> {
			if (this.selection == null || this.selection.type == null)
				return;

			this.selection.positionX = e.getX();
			this.selection.positionY = e.getY();
		});

		getChildren().add(this.canvas);
	}

	public void keyPressed(KeyCode code) {
		switch (code) {
			case SPACE:
				this.pause = !this.pause;
				break;
			case D:
				this.details = !this.details;
				break;
			case R:
				this.generate = true;
				break;
			case O:
				App.get().getStage().get().setFullScreen(false);
				this.stage2.show();
				this.stage2.requestFocus();
				break;
			case ADD:
				if (this.tau < 100)
					this.tau++;
				break;
			case SUBTRACT:
				if (this.tau > 1)
					this.tau--;
				break;
		}
	}

	public void run() {
		while (App.get().getState() != State.SHUTDOWN) {
			long t = System.currentTimeMillis();

			if (this.generate)
				this.universe.particles.clear();

			if (!this.pause) {
				if (this.generate) {
					this.generationPanel.generateParticles(this.universe);
					this.generate = false;
				}

				this.universe.tick();
			}

			Snapshot snapshot = this.universe.snapshot();

			long dt = System.currentTimeMillis() - t;
			Platform.runLater(() -> {
				GraphicsContext g = this.canvas.getGraphicsContext2D();

				long t2 = System.currentTimeMillis();
				snapshot.render(g);

				if (this.details) {
					g.setFill(Color.WHITE);
					g.fillText("Render: " + f(System.currentTimeMillis() - t2) + " ms", 10, 20);
					g.fillText("Tick: " + f(dt) + " / " + f(this.tau) + " ms", 10, 35);
					g.fillText("Particles: " + snapshot.particles.length, 10, 50);
				}
			});

			t = System.currentTimeMillis() - t;
			try {
				Thread.sleep(Math.max(1, this.tau - t));
			} catch (InterruptedException e) {
				logger.error(e);
				return;
			}
		}

		this.executor.shutdown();
	}

	private static String f(long v) {
		return v < 10 ? ("0" + v) : Long.toString(v);
	}
}
