package com.sam_chordas.android.stockhawk.ui;

import android.app.ProgressDialog;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.db.chart.Tools;
import com.db.chart.model.LineSet;
import com.db.chart.view.ChartView;
import com.db.chart.view.LineChartView;
import com.db.chart.view.animation.Animation;
import com.db.chart.view.animation.easing.CircEase;
import com.sam_chordas.android.stockhawk.R;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;

/**
 * Created by Andi Insanudin on 27/04/2016.
 */
public class LineGraphActivity extends AppCompatActivity {

    LineChartView lineChartView;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_graph);

        setTitle(getIntent().getExtras().getString("symbol"));

        final Drawable upArrow = ContextCompat.getDrawable(this, R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        upArrow.setColorFilter(ContextCompat.getColor(this, android.R.color.white), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        lineChartView = (LineChartView) findViewById(R.id.linechart);

        new AsyncTask<Void, Void, List<HistoricalQuote>>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                progressDialog = new ProgressDialog(LineGraphActivity.this);
                progressDialog.setMessage("Loading.. Please wait");
                progressDialog.setCancelable(false);
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
            }

            @Override
            protected List<HistoricalQuote> doInBackground(Void... params) {
                List<HistoricalQuote> historicalQuotes = new ArrayList<HistoricalQuote>();

                Calendar from = Calendar.getInstance();
                Calendar to = Calendar.getInstance();
                from.add(Calendar.DATE, -7); // from 1 year ago

                try {
                    Stock stock = YahooFinance.get(getIntent().getExtras().getString("symbol"));
                    historicalQuotes = stock.getHistory(from, to, Interval.DAILY);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return historicalQuotes;
            }

            @Override
            protected void onPostExecute(List<HistoricalQuote> historicalQuotes) {
                super.onPostExecute(historicalQuotes);

                if (historicalQuotes.size() > 0) {
//                StringBuilder stringBuilder = new StringBuilder();

                    String[] title = new String[historicalQuotes.size()];
                    float[] value = new float[historicalQuotes.size()];
                    BigDecimal[] bigDecimal = new BigDecimal[historicalQuotes.size()];

                    for (int i = 0; i < historicalQuotes.size(); i++) {
                        title[i] = convertDate(historicalQuotes.get(i).getDate());
                        value[i] = historicalQuotes.get(i).getAdjClose().floatValue();
                        bigDecimal[i] = historicalQuotes.get(i).getAdjClose();

//                    stringBuilder.append("date: " + historicalQuotes.get(i).getDate() + " adjClose: " + historicalQuotes.get(i).getAdjClose());
                    }

//                Log.d("historical", stringBuilder.toString());

                    lineChartView.setAxisBorderValues(getMinValue(bigDecimal) - 1, getMaxValue(bigDecimal) + 1, 1);

                    LineSet dataset = new LineSet(title, value);
                    dataset.setSmooth(true);
/*                    dataset.beginAt((getMinValue(bigDecimal)));
                    dataset.endAt(getMaxValue(bigDecimal));*/
                    dataset.setDashed(new float[]{10f, 10f});
                    dataset.setThickness(Tools.fromDpToPx(1.0f));
                    dataset.setDotsRadius(Tools.fromDpToPx(6.0f));
                    dataset.setDotsColor(ContextCompat.getColor(LineGraphActivity.this, R.color.material_red_700));
                    lineChartView.addData(dataset);

                    Paint gridPaint = new Paint();
                    gridPaint.setColor(ContextCompat.getColor(LineGraphActivity.this, android.R.color.darker_gray));
                    gridPaint.setStyle(Paint.Style.STROKE);
                    gridPaint.setAntiAlias(true);
                    gridPaint.setStrokeWidth(Tools.fromDpToPx(1.0f));
                    lineChartView.setGrid(ChartView.GridType.HORIZONTAL, gridPaint);

                    Animation anim = new Animation(500);
                    anim.setEasing(new CircEase());
                    lineChartView.show(anim);
                }

                progressDialog.dismiss();
            }
        }.execute();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    String convertDate(Calendar calendar) {
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
        return format1.format(calendar.getTime());
    }

    // getting the maximum value
    public static int getMaxValue(BigDecimal[] array) {
        int maxValue = array[0].intValue();
        for (int i = 1; i < array.length; i++) {
            if (array[i].intValue() > maxValue) {
                maxValue = array[i].intValue();

            }
        }
        return maxValue;
    }

    // getting the miniumum value
    public static int getMinValue(BigDecimal[] array) {
        int minValue = array[0].intValue();
        for (int i = 1; i < array.length; i++) {
            if (array[i].intValue() < minValue) {
                minValue = array[i].intValue();
            }
        }
        return minValue;
    }
}
