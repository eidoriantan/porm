package tk.porm.player.objects;

import java.io.File;

import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackListener;
import javazoom.jl.player.advanced.PlaybackEvent;

import tk.porm.player.interfaces.PlayerListener;
import tk.porm.player.interfaces.StreamListener;
import tk.porm.player.utils.SongStream;

public class SongPlayer {
	private File file;
	private AdvancedPlayer player;
	private SongStream stream;
	private int offset;
	private int percentage;
	private int pause;
	private boolean paused;
	private Thread threadPlay;
	private PlayerListener listener;

	public SongPlayer(String location) {
		file = new File(location);
		paused = true;
		pause = 0;
		threadPlay = null;
	}

	private Thread createThread() {
		return new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					stream = new SongStream(file);
					stream.setStreamListener(new StreamListener() {
						private boolean isFinished;

						@Override
						public void progress(int read, int length) {
							int gap = pause == 0 ? 0 : length - pause;
							if (listener != null) {
								int totalRead = (percentage > 0 ? offset : 0) + read + gap;
								listener.progress(totalRead, length);
								if (totalRead == length) isFinished = true;
							}
						}

						@Override
						public void beforeClose() {
							try {
								int available = stream.available();
								pause = available;
								if (isFinished) listener.onFinish();
							} catch (Exception exception) {
								exception.printStackTrace();
							}
						}
					});

					int length = stream.available();
					player = new AdvancedPlayer(stream);
					player.setPlayBackListener(new PlaybackListener() {
						@Override
						public void playbackStarted(PlaybackEvent event) {
							paused = false;
							if (listener != null) listener.onStart();
						}

						@Override
						public void playbackFinished(PlaybackEvent event) {
							paused = true;
							if (listener != null) listener.onStop();
						}
					});

					if (pause > 0) stream.skip(length - pause);
					else if (percentage >= 0) {
						offset = (int) ((float) length * ((float) percentage / 100));
						stream.skip(offset);
					}

					player.play();
				} catch (Exception exception) {
					exception.printStackTrace();
				}
			}
		});
	}

	public void setPlayerListener (PlayerListener listener) {
		this.listener = listener;
	}

	public void play() {
		play(0);
	}

	public void play(int percentage) {
		if (paused) {
			this.percentage = percentage;
			paused = false;
			pause = percentage > 0 ? 0 : pause;
			threadPlay = createThread();
			threadPlay.start();
		}
	}

	public void pause() {
		if (!paused) {
			paused = true;
			try {
				pause = stream.available();
				player.stop();
				threadPlay = null;
			} catch (Exception exception) {
				exception.printStackTrace();
			}
		}
	}

	public boolean isPaused() {
		return paused;
	}

	public void terminate() {
		if (player != null) {
			player.close();
		}
	}
}
