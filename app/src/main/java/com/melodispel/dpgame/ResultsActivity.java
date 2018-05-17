package com.melodispel.dpgame;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.melodispel.dpgame.data.DBContract;

public class ResultsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        WebView webView = findViewById(R.id.webViewResultsGRaph);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.requestFocusFromTouch();
        webView.loadDataWithBaseURL( "file:///android_asset/", getContent(), "text/html", "utf-8", null );
    }

    private String getContent() {

        Cursor cursor = getContentResolver().query(DBContract.ResponsesEntry.buildResponseAverageslUri(),
                null,
                null,
                null,
                null);

        StringBuilder stringRT = new StringBuilder();
        stringRT.append("[['Level', 'RT'],");

        while (cursor.moveToNext()) {
            stringRT.append("[");
            stringRT.append(cursor.getInt(2));
            stringRT.append(", ");
            stringRT.append(cursor.getInt(0));
            stringRT.append("]");
            if (!cursor.isLast()) {
                stringRT.append(",");
            }
        }
        stringRT.append("]");

        StringBuilder stringAcc = new StringBuilder();
        stringAcc.append("[['Level', 'Accuracy'],");

        while (cursor.moveToNext()) {
            stringAcc.append("[");
            stringAcc.append(cursor.getInt(2));
            stringAcc.append(", ");
            double accuracy = Math.round((cursor.getDouble(1) * 100));
            stringAcc.append(accuracy);
            stringAcc.append("]");
            if (!cursor.isLast()) {
                stringAcc.append(",");
            }
        }
        stringAcc.append("]");

        Log.i("ResultsActivity", stringRT.toString());

        String content = "<html>"
                + "  <head>"
                + "    <script type=\"text/javascript\" src=\"jsapi.js\"></script>"
                + "    <script type=\"text/javascript\">"
                + "      google.load(\"visualization\", \"1\", {packages:[\"corechart\"]});"
                + "      google.setOnLoadCallback(drawChart);"
                + "      function drawChart() {"
                + "        var data = google.visualization.arrayToDataTable(" + stringRT.toString() + ");"
                + "        var options = {"
                + "          title: 'Resposne time results',"
                + "          hAxis: {title: 'Level', titleTextStyle: {color: 'red'}}"
                + "        };"
                + "        var chart = new google.visualization.ColumnChart(document.getElementById('chart_div'));"
                + "        chart.draw(data, options);"
                + "      }"
                + "    </script>"
                + "  </head>"
                + "  <body>"
                + "    <div id=\"chart_div\" style=\"width: 1000px; height: 500px;\"></div>"
                + "  </body>" + "</html>";

        return content;
    }

}
