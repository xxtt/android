package com.example.xx.placeinspace;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.example.xml.Place;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class YouTubePlayerActivity extends YouTubeFailureRecoveryActivity implements
        YouTubePlayer.OnFullscreenListener {

    private LinearLayout baseLayout;
    private YouTubePlayerView playerView;
    private YouTubePlayer player;
    private boolean fullscreen;
    public Place place;


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_call:
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + place.getPhone()));
                startActivity(intent);
                return true;
            case R.id.action_place_share:
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, place.getTitle() + "\n" + getResources().getText(R.string.share_place_text) + "\n" + place.getLink());
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.send_place_to)));
                return true;
            case R.id.action_directions:
                Intent resultIntent = new Intent();
                setResult(RESULT_OK, resultIntent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.youtube_player_layout);

        place = (Place) getIntent().getParcelableExtra(MapsActivity.PLACE);

        getActionBar().setTitle(place.getTitle());
        getActionBar().setIcon(place.getResourceId());


        baseLayout = (LinearLayout) findViewById(R.id.youtube);
        playerView = (YouTubePlayerView) findViewById(R.id.player);
        playerView.initialize(DeveloperKey.DEVELOPER_KEY, this);

        TextView text = (TextView) findViewById(R.id.text);
        text.setText(place.getText());

        TextView address = (TextView) findViewById(R.id.address_text);
        address.setText(place.getAddress());

        TextView phone = (TextView) findViewById(R.id.phone_text);
        phone.setText(place.getPhone());

        TextView link = (TextView) findViewById(R.id.link);
        link.setText(place.getLink());
        link.setPaintFlags(link.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(place.getLink())));
            }
        });

        if (!place.getNews().isEmpty()) {
            TextView news = (TextView) findViewById(R.id.news_text);
            news.setText(place.getNews());
        }
        ImageView smoking = (ImageView) findViewById(R.id.smoking);
        if (place.isSmoking()) smoking.setImageResource(R.drawable.ic_action_smoking);
        else smoking.setImageResource(R.drawable.ic_action_no_smoking);

        if (place.isBaby()) {
            ImageView baby = (ImageView) findViewById(R.id.baby);
            baby.setImageResource(R.drawable.ic_action_baby);
        }
        if (place.isParking()) {
            ImageView parking = (ImageView) findViewById(R.id.parking);
            parking.setImageResource(R.drawable.ic_action_parking);
        }

        if (place.isMusic()) {
            ImageView music = (ImageView) findViewById(R.id.music);
            music.setImageResource(R.drawable.ic_action_music);
        }

        doLayout();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.yt_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player,
                                        boolean wasRestored) {
        this.player = player;
        player.addFullscreenControlFlag(YouTubePlayer.FULLSCREEN_FLAG_CUSTOM_LAYOUT);
        player.setOnFullscreenListener(this);
        if (!wasRestored) {
            player.loadVideo(place.getYouTubeId());
        }
    }

    @Override
    protected YouTubePlayer.Provider getYouTubePlayerProvider() {
        return playerView;
    }

    private void doLayout() {
        LinearLayout.LayoutParams playerParams =
                (LinearLayout.LayoutParams) playerView.getLayoutParams();
        if (fullscreen) {
            playerParams.width = LayoutParams.MATCH_PARENT;
            playerParams.height = LayoutParams.MATCH_PARENT;
        } else {
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                playerParams.width = 0;
                playerParams.height = WRAP_CONTENT;
                playerParams.weight = 1;
                baseLayout.setOrientation(LinearLayout.HORIZONTAL);
            } else {
                playerParams.width = MATCH_PARENT;
                playerParams.height = WRAP_CONTENT;
                playerParams.weight = 0;
                baseLayout.setOrientation(LinearLayout.VERTICAL);
            }
        }
    }

    @Override
    public void onFullscreen(boolean isFullscreen) {
        fullscreen = isFullscreen;
        doLayout();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //      this.finish();
        return true;
    }

    private static final int RECOVERY_DIALOG_REQUEST = 1;

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider,
                                        YouTubeInitializationResult errorReason) {
        if (errorReason.isUserRecoverableError()) {
            errorReason.getErrorDialog(this, RECOVERY_DIALOG_REQUEST).show();
        } else {
            String errorMessage = String.format(getString(R.string.error_player), errorReason.toString());
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RECOVERY_DIALOG_REQUEST) {
            getYouTubePlayerProvider().initialize(DeveloperKey.DEVELOPER_KEY, this);
        }
    }
}