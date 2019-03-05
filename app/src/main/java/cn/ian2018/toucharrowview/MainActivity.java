package cn.ian2018.toucharrowview;

import android.graphics.PointF;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TouchArrowView touchArrowView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Button bt_go = findViewById(R.id.bt_go);
        final Button bt_back = findViewById(R.id.bt_back);
        bt_back.setEnabled(false);
        bt_go.setEnabled(false);
        bt_go.setOnClickListener(this);
        bt_back.setOnClickListener(this);



        touchArrowView = findViewById(R.id.touch_arrow);
        touchArrowView.setOpenTouch(true);
        touchArrowView.setOnCanGoBackGoForwardListener(new TouchArrowView.OnArrowViewEventListener() {
            @Override
            public void onTouchUp(List<List<PointF>> pointList) {
            }
            @Override
            public void canGoBackGoForward(boolean canGoBack, boolean canGoForward) {
                bt_back.setEnabled(canGoBack);
                bt_go.setEnabled(canGoForward);
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (touchArrowView.isDrawing()) return;
        switch (v.getId()) {
            case R.id.bt_go:
                touchArrowView.goForward();
                break;
            case R.id.bt_back:
                touchArrowView.goBack();
                break;
        }
    }
}
