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

package net.smoofyuniverse.chaos.ui;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import net.smoofyuniverse.chaos.background.BackgroundGenerator;
import net.smoofyuniverse.chaos.background.SpaceGenerator;
import net.smoofyuniverse.chaos.universe.Particle;
import net.smoofyuniverse.chaos.universe.Snapshot;
import net.smoofyuniverse.chaos.universe.Universe;
import net.smoofyuniverse.common.app.Application;
import net.smoofyuniverse.common.app.State;
import net.smoofyuniverse.logger.core.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserInterface extends StackPane {
	private static final Logger logger = Logger.get("UserInterface");

	private final GenerationPanel generationPanel = new GenerationPanel();
	private final Stage stage2 = new Stage();

	private final Canvas canvas = new Canvas();
	private final Label help = new Label("Controls:\nH: Display or hide this help.\nSpace: Pause the universe.\nD: Show details.\nR: Regenerate the universe.\nO: Open options." +
			"\n+: Increase minimum tick duration.\n-: Decrease minimum tick duration.\n1 to 9: Force n ticks to process.\n0: Clear remaining forced ticks.\nF11: Fullscreen.");
	private final Label details = new Label();

	private final ExecutorService executor = Executors.newFixedThreadPool(4);
	private final Universe universe = new Universe(this.executor, 12);
	private final BackgroundGenerator backgroundGen = new SpaceGenerator(Color.BLACK, Color.BLUE);

	private final BooleanProperty showHelp = new SimpleBooleanProperty(true), showDetails = new SimpleBooleanProperty(false);
	private boolean pause = true, generate = true;
	private int forcedTicks = 0;
	private long age, tau = 25;

	public UserInterface() {
		this.stage2.setScene(new Scene(this.generationPanel));
		this.stage2.setTitle(Application.get().getTitle());
		this.stage2.getIcons().addAll(Application.get().getStage().get().getIcons());
		this.stage2.setWidth(750);
		this.stage2.setHeight(900);

		this.canvas.widthProperty().bind(widthProperty());
		this.canvas.heightProperty().bind(heightProperty());

		this.canvas.widthProperty().addListener((v, oldV, newV) -> {
			this.universe.setSizeX(Math.max(newV.doubleValue(), 1));
			resizeBackground();
		});
		this.canvas.heightProperty().addListener((v, oldV, newV) -> {
			this.universe.setSizeY(Math.max(newV.doubleValue(), 1));
			resizeBackground();
		});

		setOnMousePressed(e -> this.universe.select(e.getX(), e.getY()));
		setOnMouseReleased(e -> this.universe.deselect());
		setOnMouseDragged(e -> this.universe.moveSelection(e.getX(), e.getY()));

		this.showHelp.addListener((v, oldV, newV) -> {
			if (newV)
				getChildren().add(this.help);
			else
				getChildren().remove(this.help);
		});

		this.help.setStyle("-fx-font-size: 15;");
		this.help.setTextFill(Color.WHITE);
		this.help.setBackground(new Background(new BackgroundFill(Color.gray(0.05, 0.8), new CornerRadii(15), new Insets(-10))));

		this.showDetails.addListener((v, oldV, newV) -> {
			if (newV)
				getChildren().add(this.details);
			else
				getChildren().remove(this.details);
		});

		this.details.setTextFill(Color.WHITE);
		this.details.setBackground(new Background(new BackgroundFill(Color.gray(0.05, 0.8), new CornerRadii(10), new Insets(-7))));
		StackPane.setAlignment(this.details, Pos.TOP_LEFT);
		StackPane.setMargin(this.details, new Insets(10));

		getChildren().addAll(this.backgroundGen.getNode(), this.canvas, this.help);
	}

	private void resizeBackground() {
		this.backgroundGen.resize(this.universe.getSizeX(), this.universe.getSizeY());
	}

	public void keyTyped(char key) {
		switch (Character.toUpperCase(key)) {
			case 'H':
				this.showHelp.set(!this.showHelp.get());
				break;
			case ' ':
				this.pause = !this.pause;
				break;
			case 'D':
				this.showDetails.set(!this.showDetails.get());
				break;
			case 'R':
				this.generate = true;
				break;
			case 'O':
				Application.get().getStage().get().setFullScreen(false);
				this.stage2.show();
				this.stage2.requestFocus();
				break;
			case '+':
				if (this.tau < 100)
					this.tau++;
				break;
			case '-':
				if (this.tau > 1)
					this.tau--;
				break;
			case '0':
				this.forcedTicks = 0;
				break;
			case '1':
				this.forcedTicks += 1;
				break;
			case '2':
				this.forcedTicks += 2;
				break;
			case '3':
				this.forcedTicks += 3;
				break;
			case '4':
				this.forcedTicks += 4;
				break;
			case '5':
				this.forcedTicks += 5;
				break;
			case '6':
				this.forcedTicks += 6;
				break;
			case '7':
				this.forcedTicks += 7;
				break;
			case '8':
				this.forcedTicks += 8;
				break;
			case '9':
				this.forcedTicks += 9;
				break;
		}
	}

	public void run() {
		while (Application.get().getState() != State.SHUTDOWN) {
			long t = System.currentTimeMillis();

			if (this.generate) {
				this.universe.clear();
				this.age = 0;
			}

			if (!this.pause || this.forcedTicks != 0) {
				if (this.forcedTicks != 0)
					this.forcedTicks--;

				if (this.generate) {
					this.generationPanel.generateParticles(this.universe);
					this.generate = false;
				}

				this.universe.tick();
				this.age++;
			}

			Snapshot snapshot = this.universe.snapshot();

			long dt = System.currentTimeMillis() - t;
			Platform.runLater(() -> {
				GraphicsContext g = this.canvas.getGraphicsContext2D();

				long t2 = System.currentTimeMillis();
				g.clearRect(0, 0, snapshot.sizeX, snapshot.sizeY);
				snapshot.render(g);

				if (this.showDetails.get()) {
					double u = 0;
					if (snapshot.particles.length != 0) {
						for (Particle p : snapshot.particles)
							u += p.speedX * p.speedX + p.speedY * p.speedY;
						u /= snapshot.particles.length;
					}

					this.details.setText("Particles: " + snapshot.particles.length
							+ "\nRender: " + f(System.currentTimeMillis() - t2) + " ms"
							+ "\nTick: " + f(dt) + " / " + f(this.tau) + " ms"
							+ "\nAge: " + this.age
							+ "\nTemperature: " + ((int) (u * 500)) / 10D + " K");
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
