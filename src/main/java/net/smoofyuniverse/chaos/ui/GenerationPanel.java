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

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;
import net.smoofyuniverse.chaos.impl.gen.TypeAGenerator;
import net.smoofyuniverse.chaos.impl.gen.TypeAGenerators;
import net.smoofyuniverse.chaos.type.Type;
import net.smoofyuniverse.chaos.type.builder.ColoredTypeBuilder;
import net.smoofyuniverse.chaos.type.builder.TypeBuilder;
import net.smoofyuniverse.chaos.type.gen.TypeGenerator;
import net.smoofyuniverse.chaos.universe.Universe;
import net.smoofyuniverse.common.fxui.field.IntegerField;
import net.smoofyuniverse.common.util.GridUtil;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class GenerationPanel extends GridPane {
	private final ListView<TypeObject> types = new ListView<>();

	private final Random optionsRandom = new Random();
	private long genSeed;
	private int ct;

	public GenerationPanel() {
		defaultConfig();

		Button assignColors = new Button("Assign colors"), clear = new Button("Clear");
		ChoiceBox<TypeGenerator<?>> generator = new ChoiceBox<>();
		Button addOne = new Button("Add one"), addMany = new Button("Add many");

		TextField seed1 = new TextField(), seed2 = new TextField();
		Button rSeed1 = new Button("Random"), rSeed2 = new Button("Random");

		assignColors.setMaxWidth(Double.MAX_VALUE);
		clear.setMaxWidth(Double.MAX_VALUE);
		generator.setMaxWidth(Double.MAX_VALUE);
		addOne.setMaxWidth(Double.MAX_VALUE);
		addMany.setMaxWidth(Double.MAX_VALUE);

		seed1.setMaxWidth(Double.MAX_VALUE);
		seed2.setMaxWidth(Double.MAX_VALUE);
		rSeed1.setMaxWidth(Double.MAX_VALUE);
		rSeed2.setMaxWidth(Double.MAX_VALUE);

		this.types.setCellFactory(c -> new TypeCell());
		assignColors.setOnAction(e -> assignColors());
		clear.setOnAction(e -> this.types.getItems().clear());

		generator.setConverter(new StringConverter<TypeGenerator<?>>() {
			@Override
			public String toString(TypeGenerator<?> obj) {
				return obj.getDisplayName();
			}

			@Override
			public TypeGenerator<?> fromString(String string) {
				throw new UnsupportedOperationException();
			}
		});

		generator.getItems().addAll(TypeAGenerators.DEFAULT, TypeAGenerators.WANDERER);
		generator.getItems().addAll(TypeAGenerators.RANDOMS);
		generator.getSelectionModel().select(0);

		addOne.setOnAction(e -> {
			TypeGenerator<?> gen = generator.getSelectionModel().getSelectedItem();
			if (gen == null)
				return;

			TypeBuilder<?> b = gen.generate(this.optionsRandom);
			if (b instanceof ColoredTypeBuilder)
				((ColoredTypeBuilder) b).colorProperty().set(Color.hsb(this.optionsRandom.nextDouble() * 360, 1d, this.ct++ % 2 == 0 ? 1d : 0.6d));
			this.types.getItems().add(new TypeObject(b));
		});

		addMany.setOnAction(e -> {
			TypeGenerator<?> gen = generator.getSelectionModel().getSelectedItem();
			if (gen == null)
				return;

			int c = gen.recommendedTypes();
			int d = (160 / c) * 10;
			for (int i = 0; i < c; i++) {
				TypeBuilder<?> b = gen.generate(this.optionsRandom);
				if (b instanceof ColoredTypeBuilder)
					((ColoredTypeBuilder) b).colorProperty().set(Color.hsb(this.optionsRandom.nextDouble() * 360, 1d, this.ct++ % 2 == 0 ? 1d : 0.6d));
				TypeObject obj = new TypeObject(b);
				obj.count.setValue(d);
				this.types.getItems().add(obj);
			}
		});

		seed1.textProperty().addListener((v, oldV, newV) -> {
			long l;
			try {
				l = Long.parseLong(newV);
			} catch (NumberFormatException e) {
				l = newV.hashCode();
			}
			this.genSeed = l;
		});

		seed2.textProperty().addListener((v, oldV, newV) -> {
			long l;
			try {
				l = Long.parseLong(newV);
			} catch (NumberFormatException e) {
				l = newV.hashCode();
			}
			this.optionsRandom.setSeed(l);
		});

		rSeed1.setOnAction(e -> seed1.setText(randomSeed()));
		rSeed2.setOnAction(e -> seed2.setText(randomSeed()));

		seed1.setText(randomSeed());
		seed2.setText(randomSeed());

		add(this.types, 0, 0, 5, 1);

		add(assignColors, 0, 1);
		add(clear, 1, 1);
		add(generator, 2, 1);
		add(addOne, 3, 1);
		add(addMany, 4, 1);

		add(new Label("Generation seed:"), 0, 2);
		add(seed1, 1, 2, 3, 1);
		add(rSeed1, 4, 2);

		add(new Label("Options seed:"), 0, 3);
		add(seed2, 1, 3, 3, 1);
		add(rSeed2, 4, 3);

		setPadding(new Insets(5));
		setVgap(5);
		setHgap(5);

		getColumnConstraints().addAll(GridUtil.createColumn(18), GridUtil.createColumn(18), GridUtil.createColumn(28), GridUtil.createColumn(18), GridUtil.createColumn(18));
		getRowConstraints().addAll(GridUtil.createRow(Priority.ALWAYS), GridUtil.createRow(), GridUtil.createRow(), GridUtil.createRow());
	}

	private void defaultConfig() {
		TypeAGenerator gen = TypeAGenerators.RANDOMS.get(this.optionsRandom.nextInt(TypeAGenerators.RANDOMS.size()));
		int c = gen.recommendedTypes();
		int d = (160 / c) * 10;
		for (int i = 0; i < c; i++) {
			TypeObject obj = new TypeObject(gen.generate(this.optionsRandom));
			obj.count.setValue(d);
			this.types.getItems().add(obj);
		}
		assignColors();

		this.types.getItems().add(new TypeObject(TypeAGenerators.WANDERER.generate(this.optionsRandom)));
	}

	private void assignColors() {
		ObservableList<TypeObject> l = this.types.getItems();

		int count = 0;
		for (TypeObject obj : l) {
			if (obj.builder instanceof ColoredTypeBuilder)
				count++;
		}

		double offset = this.optionsRandom.nextDouble();

		int i = 0;
		for (TypeObject obj : l) {
			if (obj.builder instanceof ColoredTypeBuilder)
				((ColoredTypeBuilder) obj.builder).colorProperty().set(Color.hsb(((i++ / (double) count) + offset) * 360, 1d, i % 2 == 0 ? 1d : 0.6d));
		}
	}

	private static String randomSeed() {
		return Long.toString(ThreadLocalRandom.current().nextLong());
	}

	public void generateParticles(Universe universe) {
		Random r = new Random(this.genSeed);
		for (TypeObject cfg : this.types.getItems()) {
			Type type = cfg.builder.build(universe).orElse(null);
			if (type != null) {
				int c = cfg.count.getValue();
				for (int i = 0; i < c; i++)
					universe.add(type.createRandom(r));
			}
		}
	}

	public static class TypeObject {
		public final IntegerProperty count = new SimpleIntegerProperty(1);
		public final TypeBuilder<?> builder;

		public TypeObject(TypeBuilder<?> builder) {
			this.builder = builder;
		}
	}

	public class TypeCell extends ListCell<TypeObject> {
		private final Label typeName = new Label();
		private final IntegerField count = new IntegerField(0, 3000);
		private final ColorPicker colorPicker = new ColorPicker();
		private final StackPane options = new StackPane();
		private GridPane pane = new GridPane();
		private IntegerProperty curCount;
		private ObjectProperty<Color> curColor;

		public TypeCell() {
			setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

			this.typeName.setStyle("-fx-font-weight: bold;");

			Button remove = new Button("Remove");
			remove.setOnAction(e -> GenerationPanel.this.types.getItems().remove(getIndex()));

			this.count.setMaxWidth(Double.MAX_VALUE);
			this.colorPicker.setMaxWidth(Double.MAX_VALUE);
			remove.setMaxWidth(Double.MAX_VALUE);

			this.pane.add(this.typeName, 0, 0);
			this.pane.add(this.count, 1, 0);
			this.pane.add(this.colorPicker, 2, 0);
			this.pane.add(remove, 3, 0);
			this.pane.add(this.options, 0, 1, 4, 1);

			this.pane.setVgap(5);
			this.pane.setHgap(5);

			this.pane.getColumnConstraints().addAll(GridUtil.createColumn(15), GridUtil.createColumn(35), GridUtil.createColumn(35), GridUtil.createColumn(15));
			this.pane.getRowConstraints().addAll(GridUtil.createRow(), GridUtil.createRow(Priority.ALWAYS));
		}

		@Override
		public void updateIndex(int index) {
			super.updateIndex(index);
			unbindContent();
			setGraphic(index == -1 || isEmpty() ? null : updateContent());
		}

		private void unbindContent() {
			if (this.curCount != null)
				this.count.valueProperty().unbindBidirectional(this.curCount);
			if (this.curColor != null)
				this.colorPicker.valueProperty().unbindBidirectional(this.curColor);
		}

		private Node updateContent() {
			TypeObject obj = getItem();

			this.typeName.setText("Type: " + obj.builder.getTypeName());

			this.curCount = obj.count;
			this.count.valueProperty().bindBidirectional(this.curCount);

			if (obj.builder instanceof ColoredTypeBuilder) {
				this.curColor = ((ColoredTypeBuilder) obj.builder).colorProperty();
				this.colorPicker.valueProperty().bindBidirectional(this.curColor);
				this.colorPicker.setDisable(false);
			} else {
				this.colorPicker.setDisable(true);
			}

			this.options.getChildren().setAll(obj.builder.getNode());
			return this.pane;
		}

		@Override
		protected void updateItem(TypeObject item, boolean empty) {
			super.updateItem(item, empty);
			unbindContent();
			setGraphic(getIndex() == -1 || empty ? null : updateContent());
		}
	}
}
