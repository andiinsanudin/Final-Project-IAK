package com.sam_chordas.android.stockhawk.ui;

import android.database.Cursor;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.db.chart.Tools;
import com.db.chart.model.LineSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.ChartView;
import com.db.chart.view.LineChartView;
import com.db.chart.view.animation.Animation;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

/**
 * Created by Andi Insanudin on 27/04/2016.
 */
public class LineGraphActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    public static final String TAG_STOCK_SYMBOL = "symbol";
    private static final int STOCKS_LOADER = 1;

    private String currency;
    private LineChartView lineChartView;
    private TextView name, bid, change;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_graph);

        currency = getIntent().getStringExtra(TAG_STOCK_SYMBOL);

        setTitle(currency.toUpperCase());
        lineChartView = (LineChartView) findViewById(R.id.linechart);
        name = (TextView) findViewById(R.id.tv_name);
        bid = (TextView) findViewById(R.id.tv_bid);
        change = (TextView) findViewById(R.id.tv_change);

        getSupportLoaderManager().initLoader(STOCKS_LOADER, null, this);

        final Drawable upArrow = ContextCompat.getDrawable(this, R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        upArrow.setColorFilter(ContextCompat.getColor(this, android.R.color.white), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case STOCKS_LOADER:
                return new CursorLoader(this, QuoteProvider.Quotes.CONTENT_URI,
                        new String[]{QuoteColumns._ID, QuoteColumns.NAME, QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE,
                                QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.ISUP},
                        QuoteColumns.SYMBOL + " = ?",
                        new String[]{currency},
                        null);
        }

        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        try {
            if (data.getCount() != 0)
                renderChart(data);

            data.moveToFirst();

            name.setText(data.getString(data.getColumnIndexOrThrow(QuoteColumns.NAME)) + " (" + data.getString(data.getColumnIndexOrThrow(QuoteColumns.SYMBOL)) + ")");
            bid.setText(data.getString(data.getColumnIndexOrThrow(QuoteColumns.BIDPRICE)));
            change.setText(data.getString(data.getColumnIndexOrThrow(QuoteColumns.PERCENT_CHANGE)));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void renderChart(Cursor data) {

        LineSet lineSet = new LineSet();
        float minimumPrice = Float.MAX_VALUE;
        float maximumPrice = Float.MIN_VALUE;

        for (data.moveToFirst(); !data.isAfterLast(); data.moveToNext()) {
            String label = data.getString(data.getColumnIndexOrThrow(QuoteColumns.BIDPRICE));
            float price = Float.parseFloat(label.replaceAll(",","."));

            lineSet.addPoint(label, price);
            minimumPrice = Math.min(minimumPrice, price);
            maximumPrice = Math.max(maximumPrice, price);
        }

        lineSet.setFill(ContextCompat.getColor(LineGraphActivity.this, R.color.material_blue_500_trans))
                .setDotsColor(ContextCompat.getColor(LineGraphActivity.this, R.color.material_blue_700))
                .setDotsRadius(Tools.fromDpToPx(1.5f))
                .setDashed(new float[]{10f, 10f})
                .setThickness(Tools.fromDpToPx(1.0f))
                .setDashed(new float[]{10f, 10f});

        Paint gridPaint = new Paint();
        gridPaint.setColor(ContextCompat.getColor(LineGraphActivity.this, android.R.color.darker_gray));
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setAntiAlias(true);
        gridPaint.setStrokeWidth(Tools.fromDpToPx(1.0f));
        lineChartView.setGrid(ChartView.GridType.HORIZONTAL, gridPaint);

        lineChartView.setBorderSpacing(Tools.fromDpToPx(5))
                .setYLabels(AxisController.LabelPosition.OUTSIDE)
                .setXLabels(AxisController.LabelPosition.NONE)
                .setLabelsColor(ContextCompat.getColor(LineGraphActivity.this, android.R.color.black))
                .setXAxis(false)
                .setYAxis(false)
                .setAxisBorderValues(Math.round(Math.max(0f, minimumPrice - 5f)), Math.round(maximumPrice + 5f))
                .addData(lineSet);

        Animation anim = new Animation();

        if (lineSet.size() > 1)
            lineChartView.show(anim);
        else
            Toast.makeText(this, "No data", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}
