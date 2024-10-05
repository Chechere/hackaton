package com.hackaton2024.wiliwilowilu;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import java.util.Locale;

public class MyButton extends ConstraintLayout {
    private TextView sensorInstValue;
    private TextView sensorName;
    private TextView valuePercentage;

    private GradientDrawable layoutBg;

    public MyButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    void onClick(View v) {
        // TODO MAS INFORMACIÓN DETALLADA SOBRE EL SENSOR

        Intent intent = new Intent(getContext(), MoreInfoActivity.class);
        intent.putExtra("TITLE", sensorName.getText());

        getContext().startActivity(intent);
    }

    void init(@NonNull Context context, @Nullable AttributeSet attrs) {
        View v = LayoutInflater.from(context).inflate(R.layout.my_button, this, true);

        this.sensorInstValue = findViewById(R.id.SensorInstValue);
        this.sensorName = findViewById(R.id.SensorName);
        this.valuePercentage = findViewById(R.id.ValuePercentage);

        try (TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MyButton)) {
            layoutBg = (GradientDrawable) findViewById(R.id.MyButtonLayout).getBackground();

            layoutBg.setColor(a.getColor(
                    R.styleable.MyButton_bgColor,
                    ContextCompat.getColor(getContext(), R.color.morado)
            ));

            boolean usePercentage = a.getBoolean(R.styleable.MyButton_usePercentage, false);

            if(usePercentage) {
                valuePercentage.setVisibility(VISIBLE);
            }

            String name = a.getString(R.styleable.MyButton_sensorName);
            if(name != null) {
                sensorName.setText(name);
            } else {
                sensorName.setText(R.string.button_default_name);
            }

        }

        setClickable(true);
        setOnClickListener(this::onClick);
    }

    void setValue(double value) {
        String valorString = String.format(Locale.US, "%.1f", value);

        sensorInstValue.setText(valorString);
    }
}
