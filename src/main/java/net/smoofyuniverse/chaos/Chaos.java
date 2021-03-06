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

package net.smoofyuniverse.chaos;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import net.smoofyuniverse.chaos.impl.TypeABuilder;
import net.smoofyuniverse.chaos.type.builder.TypeBuilder;
import net.smoofyuniverse.chaos.ui.UserInterface;
import net.smoofyuniverse.common.app.Application;
import net.smoofyuniverse.common.app.Arguments;
import net.smoofyuniverse.common.environment.ApplicationUpdater;
import net.smoofyuniverse.common.environment.DependencyInfo;
import net.smoofyuniverse.common.environment.DependencyManager;
import net.smoofyuniverse.common.environment.source.GithubReleaseSource;

public class Chaos extends Application {
	public static final DependencyInfo FLOW_NOISE = new DependencyInfo("org.spongepowered:noise:2.0.0-SNAPSHOT", "https://repo.spongepowered.org/repository/sponge-legacy/org/spongepowered/noise/2.0.0-SNAPSHOT/noise-2.0.0-20190606.000239-1.jar", 55099, "7cdb48fa0c018537d272dc57da311138c5c1d6d3", "sha1");

	private UserInterface ui;

	public Chaos(Arguments args) {
		super(args, "Chaos", "1.0.11");
	}

	@Override
	public void init() {
		requireGUI();
		initServices();

		if (!this.devEnvironment) {
			new DependencyManager(this, FLOW_NOISE).setup();
		}

		TypeBuilder.REGISTRY.put("A", TypeABuilder::new);

		runLater(() -> {
			initStage(900, 700, "favicon.png");

			Scene scene = new Scene(this.ui = new UserInterface());
			scene.setOnKeyPressed(e -> {
				if (e.getCode() == KeyCode.F11)
					getStage().get().setFullScreen(true);
			});
			scene.setOnKeyTyped(e -> this.ui.keyTyped(e.getCharacter().charAt(0)));
			setScene(scene).show();
		});

		new Thread(this.ui::run).start();

		new ApplicationUpdater(this, new GithubReleaseSource("Yeregorix", "Chaos", null, "Chaos", getConnectionConfig())).run();
	}

	public static void main(String[] args) {
		new Chaos(Arguments.parse(args)).launch();
	}
}
