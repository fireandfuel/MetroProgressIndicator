/*
 * JavaFX 8 Indetermine skin for ProgressIndicator control,
 * inspired by Windows 8's ProgressRing.
 * Copyright (c) 2014, fireandfuel (fireandfuel<at>hotmail<dot>de)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * See the file LICENSE included with this distribution for more
 * information.
 */

package cuina.metro.skin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableBooleanProperty;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableIntegerProperty;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.geometry.NodeOrientation;
import javafx.scene.Node;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.transform.Scale;
import javafx.util.Duration;

import com.sun.javafx.css.converters.BooleanConverter;
import com.sun.javafx.css.converters.PaintConverter;
import com.sun.javafx.css.converters.SizeConverter;
import com.sun.javafx.scene.control.skin.ProgressIndicatorSkin;

/**
 * Indetermine skin for ProgressIndicator control, inspired by Windows 8's
 * ProgressRing.<br>
 * For determine progress FX's ProgressIndicator skin is used.<br>
 * <br>
 * How to apply this skin to your application:<br>
 * <ul>
 * <li>Check whether you use Java 8 as target platform.</li>
 * <li>Copy this file to your source folder.</li>
 * <li>Copy the LICENSE file to your root folder.</li>
 * <li>Add following lines as CSS property to your application:</li>
 * </ul>
 *
 * <pre>
 * <i>
 * .progress-indicator {
 * 	-fx-skin: "cuina.metro.skin.MetroProgressIndicatorSkin";
 * 	-fx-indeterminate-segment-count: 5;
 * }
 * </i>
 * </pre>
 *
 * It uses a sinus function to calculate the time interval per step.<br>
 * <br>
 * You can customize the angle per step, maximum step time, time until the next
 * segment appears, time between two spins and spinner color<br>
 * Simply add to the <i>.progress-indicator</i> CSS property:
 *
 * <pre>
 * <i>
 * 	-fx-indeterminate-angle-per-step: 40;
 * 	-fx-indeterminate-max-step-time: 300.0;
 * 	-fx-indeterminate-next-segment-time: 200.0;
 * 	-fx-indeterminate-next-spin-time: 500.0;
 * 	-fx-progress-color: BLACK;
 * </i>
 * </pre>
 */
public class MetroProgressIndicatorSkin extends ProgressIndicatorSkin // extends
// BehaviorSkinBase<ProgressIndicator,
// BehaviorBase<ProgressIndicator>>
{
	private ProgressIndicator control;

	private class MetroIndetermineSpinner extends Region
	{
		private final double timePerAngle(double x)
		{
			// t = |sin(x*PI / 360)| * time
			// return (Math.abs(Math.sin((x * Math.PI) / 360.0d)
			// * MetroProgressIndicatorSkin.this.maxStepTime.get()));

			// t = ((-cos(x*PI / 180) + 1) / 2) * time
			return (-Math.cos((x * Math.PI) / 180.0d) + 1) / 2
					* MetroProgressIndicatorSkin.this.maxStepTime.get();
		};

		private final double opacityPerAngle(double x)
		{
			if(x > 20.0d && x <= 700.0d)
			{
				return 1.0d;
			}
			return (Math.abs(Math.sin((x * Math.PI) / 30.0d)));
		}

		private IndicatorPaths pathsG;
		private boolean spinEnabled = false;
		private Paint fillOverride = null;

		private MetroIndetermineSpinner(boolean spinEnabled, Paint fillOverride)
		{
			// does not need to be a weak listener since it only listens to its
			// own property
			this.impl_treeVisibleProperty().addListener(
					MetroProgressIndicatorSkin.this.treeVisibleListener);
			this.spinEnabled = spinEnabled;
			this.fillOverride = fillOverride;

			this.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
			this.getStyleClass().setAll("spinner");

			this.pathsG = new IndicatorPaths();
			this.getChildren().add(this.pathsG);
			this.rebuild();

			this.rebuildTimeline();

		}

		public void setFillOverride(Paint fillOverride)
		{
			this.fillOverride = fillOverride;
			this.rebuild();
			this.rebuildTimeline();
			if(MetroProgressIndicatorSkin.this.indeterminateTransition != null)
			{
				MetroProgressIndicatorSkin.this.indeterminateTransition.playFromStart();
			}
		}

		public void setSpinEnabled(boolean spinEnabled)
		{
			this.spinEnabled = spinEnabled;
			this.rebuildTimeline();
		}

		private void rebuildTimeline()
		{
			if(this.spinEnabled)
			{
				if(MetroProgressIndicatorSkin.this.indeterminateTransition == null)
				{
					MetroProgressIndicatorSkin.this.indeterminateTransition = new Timeline();
					MetroProgressIndicatorSkin.this.indeterminateTransition
							.setCycleCount(Animation.INDEFINITE);
					MetroProgressIndicatorSkin.this.indeterminateTransition
							.setDelay(MetroProgressIndicatorSkin.this.UNCLIPPED_DELAY);
				} else
				{
					MetroProgressIndicatorSkin.this.indeterminateTransition.stop();
					((Timeline) MetroProgressIndicatorSkin.this.indeterminateTransition)
							.getKeyFrames().clear();
				}
				final ObservableList<KeyFrame> keyFrames = FXCollections
						.<KeyFrame> observableArrayList();

				for(int c = 0; c < MetroProgressIndicatorSkin.this.indeterminateSegmentCount.get(); c++)
				{
					keyFrames.add(new KeyFrame(Duration.millis(1), new KeyValue(this.pathsG
							.getChildren().get(c).rotateProperty(), 0), new KeyValue(this.pathsG
							.getChildren().get(c).opacityProperty(), 0)));

					double time = MetroProgressIndicatorSkin.this.nextSegmentTime.get() * c;
					for(int i = 0; i <= 720; i += MetroProgressIndicatorSkin.this.anglePerStep
							.get())
					{
						time += this.timePerAngle(i);

						keyFrames.add(new KeyFrame(Duration.millis(time), new KeyValue(this.pathsG
								.getChildren().get(c).rotateProperty(), i), new KeyValue(
								this.pathsG.getChildren().get(c).opacityProperty(), this
										.opacityPerAngle(i))));
					}
					keyFrames.add(new KeyFrame(Duration.millis(time
							+ MetroProgressIndicatorSkin.this.nextSpinTime.get()), new KeyValue(
							this.pathsG.getChildren().get(c).rotateProperty(), 0), new KeyValue(
							this.pathsG.getChildren().get(c).opacityProperty(), 0)));
				}

				((Timeline) MetroProgressIndicatorSkin.this.indeterminateTransition).getKeyFrames()
						.setAll(keyFrames);
				MetroProgressIndicatorSkin.this.indeterminateTransition.playFromStart();
			} else
			{
				if(MetroProgressIndicatorSkin.this.indeterminateTransition != null)
				{
					MetroProgressIndicatorSkin.this.indeterminateTransition.stop();
					((Timeline) MetroProgressIndicatorSkin.this.indeterminateTransition)
							.getKeyFrames().clear();
					MetroProgressIndicatorSkin.this.indeterminateTransition = null;
				}
			}
		}

		private class IndicatorPaths extends Pane
		{
			@Override
			protected double computePrefWidth(double height)
			{
				double w = 0;
				for(Node child : this.getChildren())
				{
					if(child instanceof Region)
					{
						Region region = (Region) child;
						if(region.getShape() != null)
						{
							w = Math.max(w, region.getShape().getLayoutBounds().getMaxX());
						} else
						{
							w = Math.max(w, region.prefWidth(height));
						}
					}
				}
				return w;
			}

			@Override
			protected double computePrefHeight(double width)
			{
				double h = 0;
				for(Node child : this.getChildren())
				{
					if(child instanceof Region)
					{
						Region region = (Region) child;
						if(region.getShape() != null)
						{
							h = Math.max(h, region.getShape().getLayoutBounds().getMaxY());
						} else
						{
							h = Math.max(h, region.prefHeight(width));
						}
					}
				}
				return h;
			}

			@Override
			protected void layoutChildren()
			{
				// calculate scale
				double scale = this.getWidth() / this.computePrefWidth(-1);
				for(Node child : this.getChildren())
				{
					if(child instanceof Region)
					{
						Region region = (Region) child;
						if(region.getShape() != null)
						{
							region.resize(region.getShape().getLayoutBounds().getMaxX(), region
									.getShape().getLayoutBounds().getMaxY());
							region.getTransforms().setAll(new Scale(scale, scale, 0, 0));
						} else
						{
							region.autosize();
						}
					}
				}
			}
		}

		@Override
		protected void layoutChildren()
		{
			final double w = MetroProgressIndicatorSkin.this.control.getWidth()
					- MetroProgressIndicatorSkin.this.control.snappedLeftInset()
					- MetroProgressIndicatorSkin.this.control.snappedRightInset();
			final double h = MetroProgressIndicatorSkin.this.control.getHeight()
					- MetroProgressIndicatorSkin.this.control.snappedTopInset()
					- MetroProgressIndicatorSkin.this.control.snappedBottomInset();
			final double prefW = this.pathsG.prefWidth(-1);
			final double prefH = this.pathsG.prefHeight(-1);
			double scaleX = w / prefW;
			double scale = scaleX;
			if((scaleX * prefH) > h)
			{
				scale = h / prefH;
			}
			double indicatorW = prefW * scale;
			double indicatorH = prefH * scale;
			this.pathsG.resizeRelocate((w - indicatorW) / 2, (h - indicatorH) / 2, indicatorW,
					indicatorH);
		}

		private void rebuild()
		{
			// update indeterminate indicator
			final int segments = MetroProgressIndicatorSkin.this.indeterminateSegmentCount.get();
			this.pathsG.getChildren().clear();
			for(int i = 0; i < segments; i++)
			{
				Region region = new Region();
				region.setScaleShape(false);
				region.setCenterShape(false);
				region.getStyleClass().addAll("segment", "segment6");
				if(this.fillOverride instanceof Color)
				{
					Color c = (Color) this.fillOverride;
					region.setStyle("-fx-background-color: rgba(" + ((int) (255 * c.getRed()))
							+ "," + ((int) (255 * c.getGreen())) + ","
							+ ((int) (255 * c.getBlue())) + "," + c.getOpacity() + ");");
				} else
				{
					region.setStyle(null);
				}
				this.pathsG.getChildren().add(region);
			}
		}
	}

	public MetroProgressIndicatorSkin(ProgressIndicator control)
	{
		super(control);
		this.control = control;

		this.control.indeterminateProperty().addListener(this.indeterminateListener);

		this.initialize();
	}

	/***************************************************************************
	 * CSS properties
	 **************************************************************************/

	/**
	 * The colour of the progress segment.
	 */
	private ObjectProperty<Paint> progressColor = new StyleableObjectProperty<Paint>(null)
	{
		@Override
		protected void invalidated()
		{
			final Paint value = this.get();
			if(value != null && !(value instanceof Color))
			{
				if(this.isBound())
				{
					this.unbind();
				}
				this.set(null);
				throw new IllegalArgumentException("Only Color objects are supported");
			}
			if(MetroProgressIndicatorSkin.this.spinner != null)
			{
				MetroProgressIndicatorSkin.this.spinner.setFillOverride(value);
			}
		}

		@Override
		public Object getBean()
		{
			return MetroProgressIndicatorSkin.this;
		}

		@Override
		public String getName()
		{
			return "progressColorProperty";
		}

		@Override
		public CssMetaData<ProgressIndicator, Paint> getCssMetaData()
		{
			return MetroProgressIndicatorSkin.PROGRESS_COLOR;
		}
	};

	Paint getProgressColor()
	{
		return this.progressColor.get();
	}

	/**
	 * The number of segments in the spinner.
	 */
	private IntegerProperty indeterminateSegmentCount = new StyleableIntegerProperty(5)
	{
		@Override
		protected void invalidated()
		{
			if(MetroProgressIndicatorSkin.this.spinner != null)
			{
				MetroProgressIndicatorSkin.this.spinner.rebuild();
				MetroProgressIndicatorSkin.this.spinner.rebuildTimeline();
				if(MetroProgressIndicatorSkin.this.indeterminateTransition != null)
				{
					MetroProgressIndicatorSkin.this.indeterminateTransition.playFromStart();
				}
			}
		}

		@Override
		public Object getBean()
		{
			return MetroProgressIndicatorSkin.this;
		}

		@Override
		public String getName()
		{
			return "indeterminateSegmentCount";
		}

		@Override
		public CssMetaData<ProgressIndicator, Number> getCssMetaData()
		{
			return MetroProgressIndicatorSkin.INDETERMINATE_SEGMENT_COUNT;
		}
	};

	/**
	 * True if the progress indicator should rotate as well as animate opacity.
	 */
	private final BooleanProperty spinEnabled = new StyleableBooleanProperty(false)
	{
		@Override
		protected void invalidated()
		{
			if(MetroProgressIndicatorSkin.this.spinner != null)
			{
				MetroProgressIndicatorSkin.this.spinner.setSpinEnabled(this.get());
			}
		}

		@Override
		public CssMetaData<ProgressIndicator, Boolean> getCssMetaData()
		{
			return MetroProgressIndicatorSkin.SPIN_ENABLED;
		}

		@Override
		public Object getBean()
		{
			return MetroProgressIndicatorSkin.this;
		}

		@Override
		public String getName()
		{
			return "spinEnabled";
		}
	};

	private final IntegerProperty anglePerStep = new StyleableIntegerProperty(10)
	{
		@Override
		protected void invalidated()
		{
			if(MetroProgressIndicatorSkin.this.spinner != null)
			{
				MetroProgressIndicatorSkin.this.spinner.rebuild();
				MetroProgressIndicatorSkin.this.spinner.rebuildTimeline();
				if(MetroProgressIndicatorSkin.this.indeterminateTransition != null)
				{
					MetroProgressIndicatorSkin.this.indeterminateTransition.playFromStart();
				}
			}
		}

		@Override
		public String getName()
		{
			return "anglePerStep";
		}

		@Override
		public Object getBean()
		{
			return MetroProgressIndicatorSkin.this;
		}

		@Override
		public CssMetaData<? extends Styleable, Number> getCssMetaData()
		{
			return MetroProgressIndicatorSkin.ANGLE_PER_STEP;
		}
	};

	private final DoubleProperty maxStepTime = new StyleableDoubleProperty(100.0d)
	{
		@Override
		protected void invalidated()
		{
			if(MetroProgressIndicatorSkin.this.spinner != null)
			{
				MetroProgressIndicatorSkin.this.spinner.rebuild();
				MetroProgressIndicatorSkin.this.spinner.rebuildTimeline();
				if(MetroProgressIndicatorSkin.this.indeterminateTransition != null)
				{
					MetroProgressIndicatorSkin.this.indeterminateTransition.playFromStart();
				}
			}
		}

		@Override
		public String getName()
		{
			return "maxStepTime";
		}

		@Override
		public Object getBean()
		{
			return MetroProgressIndicatorSkin.this;
		}

		@Override
		public CssMetaData<? extends Styleable, Number> getCssMetaData()
		{
			return MetroProgressIndicatorSkin.MAX_STEP_TIME;
		}
	};

	private final DoubleProperty nextSegmentTime = new StyleableDoubleProperty(200.0d)
	{
		@Override
		protected void invalidated()
		{
			if(MetroProgressIndicatorSkin.this.spinner != null)
			{
				MetroProgressIndicatorSkin.this.spinner.rebuild();
				MetroProgressIndicatorSkin.this.spinner.rebuildTimeline();
				if(MetroProgressIndicatorSkin.this.indeterminateTransition != null)
				{
					MetroProgressIndicatorSkin.this.indeterminateTransition.playFromStart();
				}
			}
		}

		@Override
		public String getName()
		{
			return "nextSegmentTime";
		}

		@Override
		public Object getBean()
		{
			return MetroProgressIndicatorSkin.this;
		}

		@Override
		public CssMetaData<? extends Styleable, Number> getCssMetaData()
		{
			return MetroProgressIndicatorSkin.NEXT_SEGMENT_TIME;
		}
	};

	private final DoubleProperty nextSpinTime = new StyleableDoubleProperty(500.0d)
	{
		@Override
		protected void invalidated()
		{
			if(MetroProgressIndicatorSkin.this.spinner != null)
			{
				MetroProgressIndicatorSkin.this.spinner.rebuild();
				MetroProgressIndicatorSkin.this.spinner.rebuildTimeline();
				if(MetroProgressIndicatorSkin.this.indeterminateTransition != null)
				{
					MetroProgressIndicatorSkin.this.indeterminateTransition.playFromStart();
				}
			}
		}

		@Override
		public String getName()
		{
			return "nextSpinTime";
		}

		@Override
		public Object getBean()
		{
			return MetroProgressIndicatorSkin.this;
		}

		@Override
		public CssMetaData<? extends Styleable, Number> getCssMetaData()
		{
			return MetroProgressIndicatorSkin.NEXT_SPIN_TIME;
		}
	};

	private MetroIndetermineSpinner spinner;

	@Override
	protected void initialize()
	{
		if(this.control != null)
		{
			if(this.control.isIndeterminate())
			{
				this.spinner = new MetroIndetermineSpinner(this.spinEnabled.get(),
						this.progressColor.get());
				this.getChildren().setAll(this.spinner);
				if(this.control.impl_isTreeVisible())
				{
					if(this.indeterminateTransition != null)
					{
						this.indeterminateTransition.play();
					}
				}
			} else
			{
				if(this.spinner != null)
				{
					if(this.indeterminateTransition != null)
					{
						this.indeterminateTransition.stop();
					}
					this.spinner = null;
				}

				super.initialize();
			}
		}
	}

	/***************************************************************************
	 * Stylesheet Handling
	 **************************************************************************/

	private static final CssMetaData<ProgressIndicator, Paint> PROGRESS_COLOR = new CssMetaData<ProgressIndicator, Paint>(
			"-fx-progress-color", PaintConverter.getInstance(), null)
	{

		@Override
		public boolean isSettable(ProgressIndicator n)
		{
			final MetroProgressIndicatorSkin skin = (MetroProgressIndicatorSkin) n.getSkin();
			return skin.progressColor == null || !skin.progressColor.isBound();
		}

		@Override
		public StyleableProperty<Paint> getStyleableProperty(ProgressIndicator n)
		{
			final MetroProgressIndicatorSkin skin = (MetroProgressIndicatorSkin) n.getSkin();
			return (StyleableProperty<Paint>) skin.progressColor;
		}
	};

	private static final CssMetaData<ProgressIndicator, Boolean> SPIN_ENABLED = new CssMetaData<ProgressIndicator, Boolean>(
			"-fx-spin-enabled", BooleanConverter.getInstance(), Boolean.FALSE)
	{

		@Override
		public boolean isSettable(ProgressIndicator node)
		{
			final MetroProgressIndicatorSkin skin = (MetroProgressIndicatorSkin) node.getSkin();
			return skin.spinEnabled == null || !skin.spinEnabled.isBound();
		}

		@Override
		public StyleableProperty<Boolean> getStyleableProperty(ProgressIndicator node)
		{
			final MetroProgressIndicatorSkin skin = (MetroProgressIndicatorSkin) node.getSkin();
			return (StyleableProperty<Boolean>) skin.spinEnabled;
		}
	};

	private static final CssMetaData<ProgressIndicator, Number> INDETERMINATE_SEGMENT_COUNT = new CssMetaData<ProgressIndicator, Number>(
			"-fx-indeterminate-segment-count", SizeConverter.getInstance(), 5)
	{

		@Override
		public boolean isSettable(ProgressIndicator n)
		{
			final MetroProgressIndicatorSkin skin = (MetroProgressIndicatorSkin) n.getSkin();
			return skin.indeterminateSegmentCount == null
					|| !skin.indeterminateSegmentCount.isBound();
		}

		@Override
		public StyleableProperty<Number> getStyleableProperty(ProgressIndicator n)
		{
			final MetroProgressIndicatorSkin skin = (MetroProgressIndicatorSkin) n.getSkin();
			return (StyleableProperty<Number>) skin.indeterminateSegmentCount;
		}
	};

	private static final CssMetaData<ProgressIndicator, Number> ANGLE_PER_STEP = new CssMetaData<ProgressIndicator, Number>(
			"-fx-indeterminate-angle-per-step", SizeConverter.getInstance(), 40)
	{

		@Override
		public boolean isSettable(ProgressIndicator n)
		{
			final MetroProgressIndicatorSkin skin = (MetroProgressIndicatorSkin) n.getSkin();
			return skin.anglePerStep == null || !skin.anglePerStep.isBound();
		}

		@Override
		public StyleableProperty<Number> getStyleableProperty(ProgressIndicator n)
		{
			final MetroProgressIndicatorSkin skin = (MetroProgressIndicatorSkin) n.getSkin();
			return (StyleableProperty<Number>) skin.anglePerStep;
		}
	};

	private static final CssMetaData<ProgressIndicator, Number> MAX_STEP_TIME = new CssMetaData<ProgressIndicator, Number>(
			"-fx-indeterminate-max-step-time", SizeConverter.getInstance(), 300.0d)
	{

		@Override
		public boolean isSettable(ProgressIndicator n)
		{
			final MetroProgressIndicatorSkin skin = (MetroProgressIndicatorSkin) n.getSkin();
			return skin.maxStepTime == null || !skin.maxStepTime.isBound();
		}

		@Override
		public StyleableProperty<Number> getStyleableProperty(ProgressIndicator n)
		{
			final MetroProgressIndicatorSkin skin = (MetroProgressIndicatorSkin) n.getSkin();
			return (StyleableProperty<Number>) skin.maxStepTime;
		}
	};

	private static final CssMetaData<ProgressIndicator, Number> NEXT_SEGMENT_TIME = new CssMetaData<ProgressIndicator, Number>(
			"-fx-indeterminate-next-segment-time", SizeConverter.getInstance(), 200.0d)
	{

		@Override
		public boolean isSettable(ProgressIndicator n)
		{
			final MetroProgressIndicatorSkin skin = (MetroProgressIndicatorSkin) n.getSkin();
			return skin.nextSegmentTime == null || !skin.nextSegmentTime.isBound();
		}

		@Override
		public StyleableProperty<Number> getStyleableProperty(ProgressIndicator n)
		{
			final MetroProgressIndicatorSkin skin = (MetroProgressIndicatorSkin) n.getSkin();
			return (StyleableProperty<Number>) skin.nextSegmentTime;
		}
	};

	private static final CssMetaData<ProgressIndicator, Number> NEXT_SPIN_TIME = new CssMetaData<ProgressIndicator, Number>(
			"-fx-indeterminate-next-spin-time", SizeConverter.getInstance(), 500.0d)
	{

		@Override
		public boolean isSettable(ProgressIndicator n)
		{
			final MetroProgressIndicatorSkin skin = (MetroProgressIndicatorSkin) n.getSkin();
			return skin.nextSpinTime == null || !skin.nextSpinTime.isBound();
		}

		@Override
		public StyleableProperty<Number> getStyleableProperty(ProgressIndicator n)
		{
			final MetroProgressIndicatorSkin skin = (MetroProgressIndicatorSkin) n.getSkin();
			return (StyleableProperty<Number>) skin.nextSpinTime;
		}
	};

	public static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;
	static
	{
		final List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<CssMetaData<? extends Styleable, ?>>(
				SkinBase.getClassCssMetaData());
		styleables.add(MetroProgressIndicatorSkin.PROGRESS_COLOR);
		styleables.add(MetroProgressIndicatorSkin.INDETERMINATE_SEGMENT_COUNT);
		styleables.add(MetroProgressIndicatorSkin.SPIN_ENABLED);
		styleables.add(MetroProgressIndicatorSkin.ANGLE_PER_STEP);
		styleables.add(MetroProgressIndicatorSkin.MAX_STEP_TIME);
		styleables.add(MetroProgressIndicatorSkin.NEXT_SEGMENT_TIME);
		styleables.add(MetroProgressIndicatorSkin.NEXT_SPIN_TIME);
		STYLEABLES = Collections.unmodifiableList(styleables);
	}

	/**
	 * @return The CssMetaData associated with this class, which may include the
	 *         CssMetaData of its super classes.
	 */
	public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData()
	{
		return MetroProgressIndicatorSkin.STYLEABLES;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<CssMetaData<? extends Styleable, ?>> getCssMetaData()
	{
		return MetroProgressIndicatorSkin.getClassCssMetaData();
	}

	// Listen to ProgressIndicator indeterminateProperty
	private final InvalidationListener indeterminateListener = valueModel -> this.initialize();
}
