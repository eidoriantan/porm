package tk.porm.player;

import java.util.ArrayList;
import java.io.File;
import java.io.ByteArrayInputStream;
import java.net.URL;
import java.nio.file.Paths;
import java.sql.Connection;

import java.awt.EventQueue;
import java.awt.image.BufferedImage;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JProgressBar;
import javax.swing.JFileChooser;
import javax.swing.UIManager;
import javax.swing.ImageIcon;
import javax.swing.BorderFactory;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.ListSelectionModel;
import javax.swing.LookAndFeel;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;

import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.ID3v2;

import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.FlatDarkLaf;

import tk.porm.player.database.DatabaseConnection;
import tk.porm.player.interfaces.PlayerListener;
import tk.porm.player.objects.Song;
import tk.porm.player.objects.Songs;
import tk.porm.player.objects.SongPlayer;
import tk.porm.player.objects.Settings;
import tk.porm.player.utils.ImageMap;
import tk.porm.player.utils.ImagePanel;
import tk.porm.player.utils.SystemBrowser;

public class App {
	private JFrame frame;
	private SongPlayer player;
	private Songs songs;
	private ArrayList<Song> songsList;
	private int selected;
	private boolean playing;

	private DefaultTableModel tableModel;
	private ImagePanel albumImgPane;
	private JLabel labelTitle;
	private JLabel labelArtist;
	private JButton btnPrev;
	private JButton btnNext;
	private JButton btnTogglePlay;
	private JButton btnRepeat;
	private JButton btnShuffle;
	private JButton btnHeart;
	private JTextField tfSearch;
	private JProgressBar progressBar;

	private Settings settings;
	private Settings.THEME theme;
	private Settings.REPEAT repeat;
	private boolean shuffle;

	private ImageMap mapImage;
	private ImageIcon imgPrev;
	private ImageIcon imgNext;
	private ImageIcon imgPause;
	private ImageIcon imgPlay;
	private ImageIcon imgAlbum;
	private ImageIcon imgHeart;
	private ImageIcon imgNoHeart;
	private ImageIcon imgRepeat;
	private ImageIcon imgRepeat1;
	private ImageIcon imgNoRepeat;
	private ImageIcon imgShuffle;
	private ImageIcon imgNoShuffle;
	private int albumWidth;
	private int albumHeight;

	public static void main(String[] args) {
		final DatabaseConnection dc = new DatabaseConnection();
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					App app = new App(dc.connection);
					app.frame.setVisible(true);
				} catch (Exception exception) {
					exception.printStackTrace();
				}
			}
		});
	}

	public App(Connection connection) {
		ClassLoader loader = getClass().getClassLoader();
		this.songs = new Songs(connection);
		this.settings = new Settings(connection);
		this.theme = settings.getTheme();
		this.repeat = settings.getRepeat();
		this.shuffle = settings.getShuffle();
		this.mapImage = new ImageMap(loader);
		this.selected = -1;
		this.playing = false;

		URL iconRes = loader.getResource("icon.png");
		String iconPath = iconRes.getPath();
		ImageIcon imgIcon = new ImageIcon(iconPath);
		Image icon = imgIcon.getImage();

		URL resource = loader.getResource("album.jpg");
		String imagePath = resource.getPath();
		imgAlbum = new ImageIcon(imagePath);

		frame = new JFrame();
		frame.setTitle("Player/Organizer Music");
		frame.setIconImage(icon);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setBounds(100, 100, 720, 500);

		JMenuBar menuMainBar = new JMenuBar();
		JMenu menuFile = new JMenu("File");
		JMenu menuView = new JMenu("View");
		JMenu menuAbout = new JMenu("About");
		menuFile.setMnemonic(KeyEvent.VK_F);
		menuView.setMnemonic(KeyEvent.VK_V);
		menuAbout.setMnemonic(KeyEvent.VK_A);

		JMenuItem menuAddFile = new JMenuItem("Add files...");
		menuAddFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addSongs();
			}
		});

		JMenuItem menuExit = new JMenuItem("Exit");
		menuExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.dispose();
			}
		});

		final JCheckBoxMenuItem menuTheme = new JCheckBoxMenuItem("Dark Mode");
		menuTheme.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean isDark = menuTheme.isSelected();
				settings.setTheme(isDark ? Settings.THEME.DARK : Settings.THEME.LIGHT);
				theme = settings.getTheme();
				updateTheme();
				menuTheme.setSelected(theme == Settings.THEME.DARK);
			}
		});
		menuTheme.setSelected(theme == Settings.THEME.DARK);

		JMenuItem menuYTDownloader = new JMenuItem("Youtube Downloader");
		menuYTDownloader.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SystemBrowser.open("https://yt-downloader.eidoriantan.me");
			}
		});

		JMenuItem menuAboutUs = new JMenuItem("About Us");
		menuAboutUs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AboutDialog aboutDialog = new AboutDialog(frame);
				aboutDialog.setVisible(true);
			}
		});

		menuFile.add(menuAddFile);
		menuFile.add(menuExit);
		menuView.add(menuTheme);
		menuAbout.add(menuAboutUs);
		menuAbout.add(menuYTDownloader);
		menuMainBar.add(menuFile);
		menuMainBar.add(menuView);
		menuMainBar.add(menuAbout);
		frame.setJMenuBar(menuMainBar);

		JPanel contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		frame.setContentPane(contentPane);
		contentPane.setLayout(null);

		JPanel detailsPane = new JPanel();
		detailsPane.setBounds(0, 0, 220, 460);
		detailsPane.setLayout(null);
		contentPane.add(detailsPane);

		albumWidth = imgAlbum.getIconWidth();
		albumHeight = imgAlbum.getIconHeight();
		BufferedImage bufferAlbum = new BufferedImage(albumWidth, albumHeight, BufferedImage.TYPE_INT_ARGB);
		Graphics albumGraphics = bufferAlbum.createGraphics();
		imgAlbum.paintIcon(null, albumGraphics, 0, 0);
		albumImgPane = new ImagePanel(bufferAlbum, 120, 120);
		albumImgPane.setBounds(50, 10, 120, 120);
		detailsPane.add(albumImgPane);

		labelTitle = new JLabel("SONG TITLE", SwingConstants.CENTER);
		labelTitle.setFont(labelTitle.getFont().deriveFont(labelTitle.getFont().getStyle() | Font.BOLD, 16f));
		labelTitle.setBounds(10, 140, 200, 23);
		detailsPane.add(labelTitle);

		labelArtist = new JLabel("SONG ARTIST", SwingConstants.CENTER);
		labelArtist.setFont(UIManager.getFont("Label.font"));
		labelArtist.setBounds(10, 160, 200, 23);
		detailsPane.add(labelArtist);

		progressBar = new JProgressBar();
		progressBar.setBounds(30, 350, 160, 4);
		detailsPane.add(progressBar);

		btnPrev = new JButton();
		btnPrev.setToolTipText("Play previous song");
		btnPrev.setIcon(imgPrev);
		btnPrev.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (selected == -1) return;
				selected = selected == 0 ? selected = songsList.size() - 1 : selected - 1;
				playSelected();
			}
		});
		btnPrev.setBorder(BorderFactory.createEmptyBorder());
		btnPrev.setContentAreaFilled(false);
		btnPrev.setBounds(30, 375, 30, 30);
		detailsPane.add(btnPrev);

		btnNext = new JButton();
		btnNext.setToolTipText("Play next song");
		btnNext.setIcon(imgNext);
		btnNext.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (selected == -1) return;
				selected = selected == songsList.size() - 1 ? 0 : selected + 1;
				playSelected();
			}
		});
		btnNext.setBorder(BorderFactory.createEmptyBorder());
		btnNext.setContentAreaFilled(false);
		btnNext.setBounds(160, 375, 30, 30);
		detailsPane.add(btnNext);

		btnTogglePlay = new JButton();
		btnTogglePlay.setToolTipText("Toggle song");
		btnTogglePlay.setIcon(imgPlay);
		btnTogglePlay.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (player == null) return;
				if (player.isPaused()) {
					player.play();
				} else {
					player.pause();
				}
			}
		});
		btnTogglePlay.setBorder(BorderFactory.createEmptyBorder());
		btnTogglePlay.setContentAreaFilled(false);
		btnTogglePlay.setBounds(85, 365, 50, 50);
		detailsPane.add(btnTogglePlay);

		btnRepeat = new JButton();
		btnRepeat.setToolTipText("Enable repeat");
		btnRepeat.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (repeat == Settings.REPEAT.NONE) {
					settings.setRepeat(Settings.REPEAT.ALL);
					btnRepeat.setIcon(imgRepeat);
				}

				if (repeat == Settings.REPEAT.ALL) {
					settings.setRepeat(Settings.REPEAT.ONCE);
					btnRepeat.setIcon(imgRepeat1);
				}

				if (repeat == Settings.REPEAT.ONCE) {
					settings.setRepeat(Settings.REPEAT.NONE);
					btnRepeat.setIcon(imgNoRepeat);
				}

				repeat = settings.getRepeat();
			}
		});
		btnRepeat.setBorder(BorderFactory.createEmptyBorder());
		btnRepeat.setContentAreaFilled(false);
		btnRepeat.setBounds(30, 320, 15, 15);
		detailsPane.add(btnRepeat);
		
		btnShuffle = new JButton();
		btnShuffle.setToolTipText("Shuffle list");
		btnShuffle.setIcon(imgNoShuffle);
		btnShuffle.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				settings.setShuffle(!shuffle);
				btnShuffle.setIcon(shuffle ? imgNoShuffle : imgShuffle);
				shuffle = settings.getShuffle();
			}
		});
		btnShuffle.setBorder(BorderFactory.createEmptyBorder());
		btnShuffle.setContentAreaFilled(false);
		btnShuffle.setBounds(102, 320, 15, 15);
		detailsPane.add(btnShuffle);

		btnHeart = new JButton();
		btnHeart.setToolTipText("Like/dislike song");
		btnHeart.setIcon(imgNoHeart);
		btnHeart.setBorder(BorderFactory.createEmptyBorder());
		btnHeart.setContentAreaFilled(false);
		btnHeart.setBounds(170, 320, 15, 15);
		detailsPane.add(btnHeart);

		JPanel searchPane = new JPanel();
		searchPane.setBounds(220, 0, 480, 40);
		searchPane.setLayout(null);
		contentPane.add(searchPane);

		ActionListener actionSearch = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String search = tfSearch.getText();
				loadSongs(search);
			}
		};

		tfSearch = new JTextField();
		tfSearch.addActionListener(actionSearch);
		tfSearch.setBounds(0, 10, 375, 23);
		tfSearch.setColumns(10);
		searchPane.add(tfSearch);

		JButton btnSearch = new JButton("Search");
		btnSearch.setToolTipText("Trigger search");
		btnSearch.addActionListener(actionSearch);
		btnSearch.setBounds(385, 10, 90, 23);
		searchPane.add(btnSearch);

		JPanel songsListPane = new JPanel();
		songsListPane.setBounds(220, 40, 475, 380);
		songsListPane.setLayout(null);
		contentPane.add(songsListPane);

		String[] cols = { "Album Cover", "Title", "Artist" };
		tableModel = new DefaultTableModel(cols, 0) {
			public static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable (int row, int col) {
				return false;
			}

			@Override
			public Class<?> getColumnClass(int column) {
				return column == 0 ? ImageIcon.class : super.getColumnClass(column);
			}
		};

		final JTable songsListTable = new JTable(tableModel);
		songsListTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked (MouseEvent e) {
				int clicked = e.getClickCount();
				selected = songsListTable.getSelectedRow();
				if (clicked == 2) {
					playSelected();
				}
			}
		});
		songsListTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		JTableHeader tableHeader = songsListTable.getTableHeader();
		tableHeader.setReorderingAllowed(false);
		tableHeader.setResizingAllowed(false);

		TableColumnModel model = songsListTable.getColumnModel();
		songsListTable.setRowHeight(120);
		model.getColumn(0).setPreferredWidth(20);

		JScrollPane scrollPane = new JScrollPane(songsListTable);
		scrollPane.setBounds(0, 0, 475, 380);
		songsListPane.add(scrollPane);

		JPanel actionsPane = new JPanel();
		actionsPane.setBounds(220, 420, 480, 40);
		contentPane.add(actionsPane);
		actionsPane.setLayout(null);

		JButton btnBrowse = new JButton("Add files...");
		btnBrowse.setToolTipText("Add file(s) to list");
		btnBrowse.setBounds(385, 8, 90, 23);
		btnBrowse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addSongs();
			}
		});
		actionsPane.add(btnBrowse);

		JButton btnDelete = new JButton("Remove");
		btnDelete.setToolTipText("Remove selected song");
		btnDelete.setBounds(285, 8, 90, 23);
		btnDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (selected >= 0) {
					Song selectedSong = songsList.get(selected);
					int selectedID = selectedSong.getID();
					selected = -1;
					songs.removeSong(selectedID);
					JOptionPane.showMessageDialog(frame, "Song(s) was deleted to database successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
					tfSearch.setText("");
					loadSongs("");
				}
			}
		});
		actionsPane.add(btnDelete);

		updateTheme();
		loadSongs("");
	}

	public void addSongs() {
		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("MPEG3 Songs", "mp3");
		chooser.setMultiSelectionEnabled(true);
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.addChoosableFileFilter(filter);
		chooser.showOpenDialog(frame);
		File[] files = chooser.getSelectedFiles();
		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			String location = file.getPath();
			String title = "";
			String artist = "";

			try {
				Mp3File mp3file = new Mp3File(location);
				
				if (mp3file.hasId3v2Tag()) {
					ID3v2 tags = mp3file.getId3v2Tag();
					title = tags.getTitle();
					artist = tags.getArtist();
				} else if (mp3file.hasId3v1Tag()) {
					ID3v1 tags = mp3file.getId3v1Tag();
					title = tags.getTitle();
					artist = tags.getArtist();
				}
			} catch (Exception exception) {
				exception.printStackTrace();
			}

			if (title == null || title.equals("")) {
				title = Paths.get(location).getFileName().toString();
			}

			if (artist == null) {
				artist = "";
			}

			songs.addSong(location, title, artist);
		}

		if (files.length > 0) {
			JOptionPane.showMessageDialog(frame, "Song(s) was added to database successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
			tfSearch.setText("");
			loadSongs("");
		}
	}

	public void loadSongs(String search) {
		songsList = songs.getSongs(search);
		tableModel.setRowCount(0);

		for (int i = 0; i < songsList.size(); i++) {
			Song song = songsList.get(i);
			String location = song.getLocation();
			ImageIcon album = null;

			try {
				Mp3File mp3file = new Mp3File(location);
				if (mp3file.hasId3v2Tag()) {
					ID3v2 tags = mp3file.getId3v2Tag();
					byte[] imgData = tags.getAlbumImage();

					if (imgData != null && imgData.length > 0) {
						ByteArrayInputStream stream = new ByteArrayInputStream(imgData);
						BufferedImage albumImg = ImageIO.read(stream);
						Image resized = albumImg.getScaledInstance(110, 110, Image.SCALE_SMOOTH);
						album = new ImageIcon(resized);
					}
				}
			} catch (Exception exception) {
				exception.printStackTrace();
			}

			Object[] data = { album != null ? album : imgAlbum, song.getTitle(), song.getArtist() };
			tableModel.addRow(data);
		}
	}

	public void playSelected () {
		if (selected < 0) return;

		if (player != null) {
			player.terminate();
			player = null;
		}

		btnTogglePlay.setIcon(imgPause);
		try {
			Song selectedSong = songsList.get(selected);
			String location = selectedSong.getLocation();
			String title = selectedSong.getTitle();
			String artist = selectedSong.getArtist();
			File songFile = new File(location);
			byte[] imgData = null;

			try {
				Mp3File mp3file = new Mp3File(location);
				if (mp3file.hasId3v2Tag()) {
					ID3v2 tags = mp3file.getId3v2Tag();
					imgData = tags.getAlbumImage();
				}
			} catch (Exception exception) {
				exception.printStackTrace();
			}

			labelTitle.setText(title);
			labelArtist.setText(artist);

			if (imgData != null) {
				ByteArrayInputStream stream = new ByteArrayInputStream(imgData);
				BufferedImage album = ImageIO.read(stream);
				albumImgPane.setImage(album);
			} else {
				BufferedImage album = new BufferedImage(albumWidth, albumHeight, BufferedImage.TYPE_INT_ARGB);
				Graphics albumGraphics = album.createGraphics();
				imgAlbum.paintIcon(null, albumGraphics, 0, 0);
				albumImgPane.setImage(album);
			}

			if (songFile.exists()) {
				player = new SongPlayer(location);
				player.setPlayerListener(new PlayerListener() {
					@Override
					public void progress(int read, int length) {
						progressBar.setValue(read);
						progressBar.setMaximum(length);
					}

					@Override
					public void onStart() {
						playing = true;
						btnTogglePlay.setIcon(imgPause);
					}

					@Override
					public void onStop() {
						playing = false;
						btnTogglePlay.setIcon(imgPlay);
					}
				});
				player.play();
			} else {
				JOptionPane.showMessageDialog(frame, "File was not found on the system", "Failed", JOptionPane.ERROR_MESSAGE);
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	public void updateTheme() {
		boolean isDark = theme == Settings.THEME.DARK;
		LookAndFeel laf = null;

		if (isDark) {
			laf = new FlatDarkLaf();
			imgPrev = mapImage.getIcon(ImageMap.ImageKey.PREV_DARK);
			imgNext = mapImage.getIcon(ImageMap.ImageKey.NEXT_DARK);
			imgPlay = mapImage.getIcon(ImageMap.ImageKey.PLAY_DARK);
			imgPause = mapImage.getIcon(ImageMap.ImageKey.PAUSE_DARK);
			imgHeart = mapImage.getIcon(ImageMap.ImageKey.HEART_DARK);
			imgNoHeart = mapImage.getIcon(ImageMap.ImageKey.NO_HEART_DARK);
			imgRepeat = mapImage.getIcon(ImageMap.ImageKey.REPEAT_DARK);
			imgRepeat1 = mapImage.getIcon(ImageMap.ImageKey.REPEAT_1_DARK);
			imgNoRepeat = mapImage.getIcon(ImageMap.ImageKey.NO_REPEAT_DARK);
			imgShuffle = mapImage.getIcon(ImageMap.ImageKey.SHUFFLE_DARK);
			imgNoShuffle = mapImage.getIcon(ImageMap.ImageKey.NO_SHUFFLE_DARK);
		} else {
			laf = new FlatLightLaf();
			imgPrev = mapImage.getIcon(ImageMap.ImageKey.PREV_LIGHT);
			imgNext = mapImage.getIcon(ImageMap.ImageKey.NEXT_LIGHT);
			imgPlay = mapImage.getIcon(ImageMap.ImageKey.PLAY_LIGHT);
			imgPause = mapImage.getIcon(ImageMap.ImageKey.PAUSE_LIGHT);
			imgHeart = mapImage.getIcon(ImageMap.ImageKey.HEART_LIGHT);
			imgNoHeart = mapImage.getIcon(ImageMap.ImageKey.NO_HEART_LIGHT);
			imgRepeat = mapImage.getIcon(ImageMap.ImageKey.REPEAT_LIGHT);
			imgRepeat1 = mapImage.getIcon(ImageMap.ImageKey.REPEAT_1_LIGHT);
			imgNoRepeat = mapImage.getIcon(ImageMap.ImageKey.NO_REPEAT_LIGHT);
			imgShuffle = mapImage.getIcon(ImageMap.ImageKey.SHUFFLE_LIGHT);
			imgNoShuffle = mapImage.getIcon(ImageMap.ImageKey.NO_SHUFFLE_LIGHT);
		}

		btnPrev.setIcon(imgPrev);
		btnNext.setIcon(imgNext);
		btnTogglePlay.setIcon(playing ? imgPause : imgPlay);

		if (repeat == Settings.REPEAT.NONE) btnRepeat.setIcon(imgNoRepeat);
		if (repeat == Settings.REPEAT.ALL) btnRepeat.setIcon(imgRepeat);
		if (repeat == Settings.REPEAT.ONCE) btnRepeat.setIcon(imgRepeat1);

		btnShuffle.setIcon(shuffle ? imgShuffle : imgNoShuffle);
		btnHeart.setIcon(imgNoHeart);

		try {
			UIManager.setLookAndFeel(laf);
			SwingUtilities.updateComponentTreeUI(frame);
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}
}
