/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.exoplayer2.imademo;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.Nullable;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.C.ContentType;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ext.ima.ImaAdsLoader;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MediaSourceEventListener;
import com.google.android.exoplayer2.source.MergingMediaSource;
import com.google.android.exoplayer2.source.SingleSampleMediaSource;
import com.google.android.exoplayer2.source.ads.AdsMediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;

/** Manages the {@link ExoPlayer}, the IMA plugin and all video playback. */
/* package */ final class PlayerManager implements AdsMediaSource.MediaSourceFactory {

  private final ImaAdsLoader adsLoader;
  private final DataSource.Factory manifestDataSourceFactory;
  private final DataSource.Factory mediaDataSourceFactory;

  private SimpleExoPlayer player;
  private long contentPosition;

  public PlayerManager(Context context) {
//    String adTag = context.getString(R.string.ad_tag_url);
    String adTag = VMAP_AD_URL;
    adsLoader = new ImaAdsLoader(context, Uri.parse(adTag));
    manifestDataSourceFactory =
        new DefaultDataSourceFactory(
            context, Util.getUserAgent(context, context.getString(R.string.application_name)));
    mediaDataSourceFactory =
        new DefaultDataSourceFactory(
            context,
            Util.getUserAgent(context, context.getString(R.string.application_name)),
            new DefaultBandwidthMeter());
  }

  private static final String captionUrl = "http://fs.jtbc.joins.com/joydata/CP00000001/prog/culture/ssulzun/srt/20180427_093243_129.vtt";
  private static final String VMAP_AD_URL = "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=%2F124319096%2Fexternal%2Fad_rule_samples&ciu_szs=300x250&ad_rule=1&gdfp_req=1&env=vp&output=xml_vmap1&unviewed_position_start=1&cust_params=sample_ar%3Dpremidpostpod%26deployment%3Dgmf-js&cmsid=496&vid=short_onecue&correlator=2299622947144259&eid=420706009%2C495644009&sdkv=h.3.195.4&sdki=3c0d&scor=3190932528678012&adk=195590520&u_so=l&osd=2&frm=0&sdr=1&is_amp=0&vpa=auto&vpmute=0&mpt=videojs-ima&mpv=1.2.1&afvsz=200x200%2C250x250%2C300x250%2C336x280%2C450x50%2C468x60%2C480x70&url=https%3A%2F%2Fgoogleads.github.io%2Fvideojs-ima%2Fexamples%2Fautoplay%2F&ged=ve4_td1_tt0_pd1_la1000_er0.0.0.0_vi0.0.740.1597_vp0_eb16491";

  private static final String[] uriStrings = {"https://html5demos.com/assets/dizzy.mp4", "https://storage.googleapis.com/exoplayer-test-media-1/mkv/android-screens-lavf-56.36.100-aac-avc-main-1280x720.mkv"};



  public void init(Context context, PlayerView playerView) {
    // Create a default track selector.
    BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
    TrackSelection.Factory videoTrackSelectionFactory =
        new AdaptiveTrackSelection.Factory(bandwidthMeter);
    TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

    // Create a player instance.
    player = ExoPlayerFactory.newSimpleInstance(context, trackSelector);

    // Bind the player to the view.
    playerView.setPlayer(player);

    // This is the MediaSource representing the content media (i.e. not the ad).
//    String contentUrl = context.getString(R.string.content_url);
//    MediaSource contentMediaSource = buildMediaSource(Uri.parse(contentUrl), /* handler= */ null, /* listener= */ null);

    ////// added code

    Uri[] uris= new Uri[2];
    MediaSource[] mediaSources = new MediaSource[2];
    for (int i = 0; i < 2; i++) {

      uris[i] = Uri.parse(uriStrings[i]);
      mediaSources[i] = buildMediaSource(uris[i], null, null);
      if(captionUrl != null) {
        Uri subtitleUri = Uri.parse(captionUrl);
        String mimeType = MimeTypes.APPLICATION_SUBRIP;
        if (captionUrl.endsWith(".vtt") == true)
          mimeType = MimeTypes.TEXT_VTT;

        Format subtitleFormat = Format.createTextSampleFormat(
                "0", // An identifier for the track. May be null.
                mimeType, // The mime type. Must be set correctly.
                Format.NO_VALUE, // Selection flags for the track.
                "kr");

        MediaSource textMediaSource = new SingleSampleMediaSource.Factory(mediaDataSourceFactory).createMediaSource(subtitleUri, subtitleFormat, C.SELECTION_FLAG_AUTOSELECT, null, null);
        mediaSources[i] = new MergingMediaSource(mediaSources[i], textMediaSource);
      }
    }
    MediaSource mediaSource = mediaSources.length == 1 ? mediaSources[0] : new ConcatenatingMediaSource(mediaSources);

    ////// added code end

    // Compose the content media source into a new AdsMediaSource with both ads and content.
    MediaSource mediaSourceWithAds =
        new AdsMediaSource(
                mediaSource,
            /* adMediaSourceFactory= */ this,
            adsLoader,
            playerView.getOverlayFrameLayout(),
            /* eventHandler= */ null,
            /* eventListener= */ null);

    // Prepare the player with the source.
    player.seekTo(contentPosition);
    player.prepare(mediaSourceWithAds);
    player.setPlayWhenReady(true);
  }

  public void reset() {
    if (player != null) {
      contentPosition = player.getContentPosition();
      player.release();
      player = null;
    }
  }

  public void release() {
    if (player != null) {
      player.release();
      player = null;
    }
    adsLoader.release();
  }

  // AdsMediaSource.MediaSourceFactory implementation.

  @Override
  public MediaSource createMediaSource(
      Uri uri, @Nullable Handler handler, @Nullable MediaSourceEventListener listener) {
    return buildMediaSource(uri, handler, listener);
  }

  @Override
  public int[] getSupportedTypes() {
    // IMA does not support Smooth Streaming ads.
    return new int[] {C.TYPE_DASH, C.TYPE_HLS, C.TYPE_OTHER};
  }

  // Internal methods.

  private MediaSource buildMediaSource(
      Uri uri, @Nullable Handler handler, @Nullable MediaSourceEventListener listener) {
    @ContentType int type = Util.inferContentType(uri);
    switch (type) {
      case C.TYPE_DASH:
        return new DashMediaSource.Factory(
                new DefaultDashChunkSource.Factory(mediaDataSourceFactory),
                manifestDataSourceFactory)
            .createMediaSource(uri, handler, listener);
      case C.TYPE_SS:
        return new SsMediaSource.Factory(
                new DefaultSsChunkSource.Factory(mediaDataSourceFactory), manifestDataSourceFactory)
            .createMediaSource(uri, handler, listener);
      case C.TYPE_HLS:
        return new HlsMediaSource.Factory(mediaDataSourceFactory)
            .createMediaSource(uri, handler, listener);
      case C.TYPE_OTHER:
        return new ExtractorMediaSource.Factory(mediaDataSourceFactory)
            .createMediaSource(uri, handler, listener);
      default:
        throw new IllegalStateException("Unsupported type: " + type);
    }
  }

}
