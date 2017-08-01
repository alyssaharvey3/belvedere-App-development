package com.example.belvedere;

import android.os.Bundle;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import zendesk.belvedere.BelvedereUi;
import zendesk.belvedere.ImageStream;
import zendesk.belvedere.MediaResult;

public class ChatActivity extends AppCompatActivity {

    private EditText input;

    private ImageStream imageStream;

    private Listener listener;
    private ImageStream.ScrollListener scrollListener;

    static List<MediaResult> mediaResults = new ArrayList<>();
    static Set<MediaResult> extraResults = new TreeSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_activity);
        setSupportActionBar((Toolbar) findViewById(R.id.activity_request_toolbar));
        imageStream = BelvedereUi.install(this);

        this.listener = new Listener();
        this.scrollListener = new ScrollListener();

        imageStream.addListener(listener);
        imageStream.addScrollListener(scrollListener);

        this.input = findViewById(R.id.input);

        findViewById(R.id.send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(imageStream.isAttachmentsPopupVisible()){
                    imageStream.dismiss();
                }
                mediaResults.clear();
                extraResults.clear();
                ((EditText)findViewById(R.id.input)).setText("");
                ((Button)findViewById(R.id.attachment)).setText(mediaResults.size()+"");
            }
        });

        RecyclerView recyclerView = findViewById(R.id.activity_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new FakeAdapter());

        init();
    }

    private void init() {
        if(imageStream.getKeyboardHelper().getInputTrap().hasFocus()) {
            input.requestFocus();
        }

        if(imageStream.wasOpen()) {
            input.post(new Runnable() {
                @Override
                public void run() {
                    showImageStream();
                }
            });
        }

        findViewById(R.id.attachment).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!imageStream.isAttachmentsPopupVisible()) {
                    showImageStream();
                } else {
                    imageStream.dismiss();
                }
            }
        });
    }

    private class Listener implements ImageStream.Listener {

        @Override
        public void onDismissed() {
            input.requestFocus();
        }

        @Override
        public void onVisible() {
            // Intentionally empty.
        }

        @Override
        public void onImageSelected(List<MediaResult> r, boolean replace) {

            if(replace) {
                mediaResults.clear();
            }

            mediaResults.addAll(new ArrayList<>(r));
            extraResults.addAll(new ArrayList<>(r));

            ((Button)findViewById(R.id.attachment)).setText(mediaResults.size()+"");

            if(!imageStream.isAttachmentsPopupVisible()) {
                showImageStream();
            }
        }
    }

    private class ScrollListener implements ImageStream.ScrollListener {

        @Override
        public void onScroll(int height, int scrollArea, float scrollPosition) {
            final Interpolator interpolator = PathInterpolatorCompat.create(.19f,0f,.2f,1f);
            final float interpolation = interpolator.getInterpolation((scrollPosition * .30f));
            final int bottomPadding = (int) (-1f * interpolation * scrollArea);
            findViewById(R.id.activity_input).setTranslationY(bottomPadding);
            findViewById(R.id.activity_recyclerview).setTranslationY(bottomPadding);
        }
    }

    private void showImageStream() {
        BelvedereUi
                .imageStream(ChatActivity.this)
                .resolveMedia(false)
                .withCameraIntent()
                .withDocumentIntent("*/*", true)
                .withSelectedItems(new ArrayList<>(mediaResults))
                .withExtraItems(new ArrayList<>(extraResults))
                .withTouchableItems(R.id.attachment, R.id.send)
                .withMaxFileSize(1024L * 1024L * 1024L * 10L, "File too large")
                .showPopup(ChatActivity.this);
    }

    class FakeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_1, parent, false);
            return new RecyclerView.ViewHolder(v) {};
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ((TextView)holder.itemView.findViewById(android.R.id.text1)).setText("bla");
        }

        @Override
        public int getItemCount() {
            return 1;
        }
    }
}
