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
import net.smoofyuniverse.chaos.impl.gen.TypeAGenerators;
import net.smoofyuniverse.chaos.type.Type;
import net.smoofyuniverse.chaos.type.builder.ColoredTypeBuilder;
import net.smoofyuniverse.chaos.type.builder.TypeBuilder;
import net.smoofyuniverse.chaos.type.gen.TypeGenerator;
import net.smoofyuniverse.chaos.universe.Universe;
import net.smoofyuniverse.common.fxui.field.IntegerField;
import net.smoofyuniverse.common.util.GridUtil;

import java.util.Random;

public class GenerationPanel extends GridPane {
	private final ListView<TypeObject> types = new ListView<>();

	private final Random random = new Random();
	private int ct;

	public GenerationPanel() {
		defaultConfig();

		Button assignColors = new Button("Assign colors"), clear = new Button("Clear");
		ChoiceBox<TypeGenerator<?>> generator = new ChoiceBox<>();
		Button addOne = new Button("Add one"), addMany = new Button("Add many");

		assignColors.setMaxWidth(Double.MAX_VALUE);
		clear.setMaxWidth(Double.MAX_VALUE);
		generator.setMaxWidth(Double.MAX_VALUE);
		addOne.setMaxWidth(Double.MAX_VALUE);
		addMany.setMaxWidth(Double.MAX_VALUE);

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

			TypeBuilder<?> b = gen.generate(this.random);
			if (b instanceof ColoredTypeBuilder)
				((ColoredTypeBuilder) b).colorProperty().set(Color.hsb(this.random.nextDouble() * 360, 1d, this.ct++ % 2 == 0 ? 1d : 0.6d));
			this.types.getItems().add(new TypeObject(b));
		});

		addMany.setOnAction(e -> {
			TypeGenerator<?> gen = generator.getSelectionModel().getSelectedItem();
			if (gen == null)
				return;

			int c = gen.recommendedTypes();
			int d = (200 / c) * 10;
			for (int i = 0; i < c; i++) {
				TypeBuilder<?> b = gen.generate(this.random);
				if (b instanceof ColoredTypeBuilder)
					((ColoredTypeBuilder) b).colorProperty().set(Color.hsb(this.random.nextDouble() * 360, 1d, this.ct++ % 2 == 0 ? 1d : 0.6d));
				TypeObject obj = new TypeObject(b);
				obj.count.setValue(d);
				this.types.getItems().add(obj);
			}
		});

		add(this.types, 0, 0, 3, 1);
		add(assignColors, 0, 1);
		add(clear, 1, 1);
		add(generator, 0, 2);
		add(addOne, 1, 2);
		add(addMany, 2, 2);

		setPadding(new Insets(8));
		setVgap(5);
		setHgap(5);

		getColumnConstraints().addAll(GridUtil.createColumn(33), GridUtil.createColumn(33), GridUtil.createColumn(33));
		getRowConstraints().addAll(GridUtil.createRow(Priority.ALWAYS), GridUtil.createRow(), GridUtil.createRow());
	}

	private void defaultConfig() {
		int c = TypeAGenerators.LARGE_CLUSTERS.recommendedTypes();
		int d = (200 / c) * 10;
		for (int i = 0; i < c; i++) {
			TypeObject obj = new TypeObject(TypeAGenerators.LARGE_CLUSTERS.generate(this.random));
			obj.count.setValue(d);
			this.types.getItems().add(obj);
		}
		assignColors();

		this.types.getItems().add(new TypeObject(TypeAGenerators.WANDERER.generate(this.random)));
	}

	private void assignColors() {
		ObservableList<TypeObject> l = this.types.getItems();

		int count = 0;
		for (TypeObject obj : l) {
			if (obj.builder instanceof ColoredTypeBuilder)
				count++;
		}

		double offset = this.random.nextDouble();

		int i = 0;
		for (TypeObject obj : l) {
			if (obj.builder instanceof ColoredTypeBuilder)
				((ColoredTypeBuilder) obj.builder).colorProperty().set(Color.hsb(((i++ / (double) count) + offset) * 360, 1d, i % 2 == 0 ? 1d : 0.6d));
		}
	}

	public void generateParticles(Universe universe) {
		for (TypeObject cfg : this.types.getItems()) {
			Type type = cfg.builder.build().orElse(null);
			if (type != null) {
				int c = cfg.count.getValue();
				for (int i = 0; i < c; i++)
					universe.particles.add(type.createRandom(universe, this.random));
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
		private final IntegerField count = new IntegerField(0, 3000);
		private final ColorPicker colorPicker = new ColorPicker();
		private final StackPane options = new StackPane();
		private GridPane pane = new GridPane();
		private IntegerProperty curCount;
		private ObjectProperty<Color> curColor;

		public TypeCell() {
			setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

			Button remove = new Button("Remove");
			remove.setOnAction(e -> GenerationPanel.this.types.getItems().remove(getIndex()));

			this.count.setMaxWidth(Double.MAX_VALUE);
			this.colorPicker.setMaxWidth(Double.MAX_VALUE);
			remove.setMaxWidth(Double.MAX_VALUE);

			this.pane.add(this.count, 0, 0);
			this.pane.add(this.colorPicker, 1, 0);
			this.pane.add(remove, 2, 0);
			this.pane.add(this.options, 0, 1, 3, 1);

			this.pane.setVgap(5);
			this.pane.setHgap(5);

			this.pane.getColumnConstraints().addAll(GridUtil.createColumn(40), GridUtil.createColumn(40), GridUtil.createColumn(20));
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
