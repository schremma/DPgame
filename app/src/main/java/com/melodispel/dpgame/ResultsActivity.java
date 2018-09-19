package com.melodispel.dpgame;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

import com.melodispel.dpgame.data.DBContract;
import com.melodispel.dpgame.data.DPGamePreferences;

public class ResultsActivity extends AppCompatActivity {

    private static final int COLUMN_ALL_RTs = 0;
    private static final int COLUMN_CORRECT_RTs = 0;
    private static final int COLUMN_LEVEL = 2;
    private static final int COLUMN_ACCURACY = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);
        DPGamePreferences.applyPreferredAppLanguage(this);

        WebView webView = findViewById(R.id.webViewResultsGRaph);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.requestFocusFromTouch();
        String content = getContent();
        if (content != null) {
            webView.loadDataWithBaseURL("file:///android_asset/", getContent(), "text/html", "utf-8", null);
        } else {
            TextView textView = findViewById(R.id.tv_default_message);
            textView.setText(getString(R.string.message_no_results_to_display));
            textView.setVisibility(View.VISIBLE);
        }
    }

    private String getContent() {

        Cursor cursorResponses = getContentResolver().query(DBContract.ResponsesEntry.buildResponseAverageslUri(),
                null,
                null,
                null,
                null);

        Cursor cursorCorrectRTs = getContentResolver().query(DBContract.ResponsesEntry.buildAverageCorrectOrIncorrectRTlUri(true),
                null,
                null,
                null,
                null);

        if (cursorResponses.getCount() > 1) {

            StringBuilder stringRT = new StringBuilder();
            stringRT.append("[");

            while (cursorResponses.moveToNext() && cursorCorrectRTs.moveToNext()) {
                stringRT.append("[");
                stringRT.append("'");
                stringRT.append(cursorResponses.getInt(COLUMN_LEVEL));
                stringRT.append("'");
                stringRT.append(", ");
                stringRT.append(cursorResponses.getInt(COLUMN_ALL_RTs));
                stringRT.append(", ");
                stringRT.append(cursorCorrectRTs.getInt(COLUMN_CORRECT_RTs));
                stringRT.append("]");
                if (!cursorResponses.isLast()) {
                    stringRT.append(",");
                }
            }
            stringRT.append("]");

            StringBuilder stringAcc = new StringBuilder();
            stringAcc.append("[");
            cursorResponses.moveToPosition(-1);
            while (cursorResponses.moveToNext()) {
                stringAcc.append("[");
                stringAcc.append("'");
                stringAcc.append(cursorResponses.getInt(COLUMN_LEVEL));
                stringAcc.append("'");
                stringAcc.append(", ");
                int accuracy = (int) Math.round((cursorResponses.getDouble(COLUMN_ACCURACY) * 100));
                stringAcc.append(accuracy);
                stringAcc.append("]");
                if (!cursorResponses.isLast()) {
                    stringAcc.append(",");
                }
            }
            stringAcc.append("]");

            cursorCorrectRTs.close();
            cursorResponses.close();

            Log.i("ResultsActivity", stringAcc.toString());

            String content = "<html>"
                    + "  <head>"
                    + "    <script type=\"text/javascript\" src=\"jsapi.js\"></script>"
                    + "    <script type=\"text/javascript\">"
                    + "      google.load(\"visualization\", \"1\", {packages:[\"corechart\"]});"
                    + "      google.setOnLoadCallback(drawChart);"
                    + "      function drawChart() {"
                    + "        var data = new google.visualization.DataTable();"
                    + "          data.addColumn('string', 'Level'); "
                    + "          data.addColumn('number', 'Response speed'); "
                    + "          data.addColumn('number', 'Only correct responses'); "
                    + "                    data.addRows(" + stringRT.toString() + ");"
                    + "        var options = {"
                    + "          title: 'Response speed results',"
                    + "          hAxis: {title: 'Level'}"
                    + "        };"
                    + "        var chart = new google.visualization.ColumnChart(document.getElementById('chart_div'));"
                    + "        chart.draw(data, options);"
                    + "        data = new google.visualization.DataTable();"
                    + "          data.addColumn('string', 'Level'); "
                    + "          data.addColumn('number', 'Accuracy'); "
                    + "                    data.addRows(" + stringAcc.toString() + ");"
                    + "        options = {"
                    + "          title: 'Accuracy results',"
                    + "          hAxis: {title: 'Level'},"
                    + "           vAxis: {minValue: 0}"
                    + "        };"
                    + "        var chartAcc = new google.visualization.ColumnChart(document.getElementById('chart_accuracy_div'));"
                    + "        chartAcc.draw(data, options);"
                    + "      }"
                    + "    </script>"
                    + "  </head>"
                    + "  <body>"
                    + "    <div id=\"chart_div\"></div>"
                    + "    <div id=\"chart_accuracy_div\"></div>"
                    + "  </body>" + "</html>";

            return content;
        }
        return null;
    }

}
