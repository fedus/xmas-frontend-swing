package eu.dillendapp.xmas.xmas_remote;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class XmasRemote {
	
	final private String APP_ICON_PATH = "img/christmas-tree.png";
	final private String TREE_PATH = "img/tree.png";
	
	final private String MQTT_USER = "";
	final private String MQTT_PASS = "";
	final private String MQTT_HOST = "";
	final private int MQTT_PORT = 9002;
	final private String MQTT_TOPIC = "house/xmas/control/";
	final private String MQTT_STATUS = "house/xmas/status/";
	
	
	final private int BRIGHTNESS_MIN = 0;
	final private int BRIGHTNESS_MAX  = 1024;
	final private int BRIGHTNESS_START = 50;
	
	final private int DELAY_MIN = 1;
	final private int DELAY_MAX = 1000;
	final private int DELAY_START = 10;
	
	final private int STEP_MIN = 1;
	final private int STEP_MAX = 100;
	final private int STEP_START = 1;
	
	final private int MIN_MIN = BRIGHTNESS_MIN;
	final private int MIN_MAX = BRIGHTNESS_MAX - 1;
	final private int MIN_START = BRIGHTNESS_MIN;
	
	final private int MAX_MIN = BRIGHTNESS_MIN + 1;
	final private int MAX_MAX = BRIGHTNESS_MAX;
	final private int MAX_START = BRIGHTNESS_MAX;
	
	final private int BUMP_MIN = -BRIGHTNESS_MAX;
	final private int BUMP_MAX = BRIGHTNESS_MAX;
	final private int BUMP_START = 50;
	
	final private int SPINNER_STEP = 1;
	final private int SPINNER_WIDTH = 100;
	final private int SPINNER_HEIGHT = 25;
	final private int MIN_MAX_WIDTH = 150;
	final private int MIN_MAX_HEIGHT = 15;
	
	final private String STATUS_START = "Loading ...";
	
	private JFrame frame;
	private JPanel pnl_controls, pnl_image, pnl_top, pnl_bottom, pnl_min, pnl_max, pnl_delay, pnl_step, pnl_min_max, pnl_bump, pnl_lower_bottom, pnl_quick_power;
	private JLabel lbl_mode, lbl_brightness, lbl_perc_bright, lbl_min, lbl_max, lbl_delay, lbl_step, lbl_bump, lbl_status, lbl_image;
	private JButton btn_bump, btn_quick_power_on, btn_quick_power_off;
	private JCheckBox ck_fade;
	private JComboBox<XmasMode> cmb_mode;
	private JSlider sl_brightness;
	private JSpinner sp_min, sp_max, sp_delay, sp_step, sp_bump;
	private ImageIcon img_app_icon = new ImageIcon(APP_ICON_PATH);
	private ImageIcon img_tree = new ImageIcon(TREE_PATH);
	private XmasMode[] xmas_modes = {
		new XmasMode("Static", 0, true),
		new XmasMode("Fade In-Out", 1, false),
		new XmasMode("Fade In", 4, false),
		new XmasMode("Fade Out", 2, false),
		new XmasMode("Blink", 3, false),
		new XmasMode("Mix", 5, false)
	};
	
	private SpinnerNumberModel sp_m_min, sp_m_max;
	private MqttClient client;
	private MqttConnectOptions mqtt_options;
	
	private boolean buttons_active = true;
	
	public void createGUI() {
		// Main function, creates window and displays elements.
		
		setLAF();
		
		frame = new JFrame("X-Mas Remote");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new FlowLayout());
		
		createElements();
		setupElements();
		addElements();
		addListeners();
		
		frame.pack();
		frame.setVisible(true);
		
		setupMQTT();
	}
	
	public void createElements() {
		// Create all needed elements.
		pnl_controls = new JPanel();
		pnl_image = new JPanel();
		pnl_top = new JPanel();
		pnl_bottom = new JPanel();
		pnl_bottom.setLayout(new BoxLayout(pnl_bottom, BoxLayout.Y_AXIS));
		pnl_min = new JPanel();
		pnl_max = new JPanel();
		pnl_step = new JPanel();
		pnl_delay = new JPanel();
		pnl_min_max = new JPanel(new GridLayout(2,2));
		pnl_bump = new JPanel();
		pnl_lower_bottom = new JPanel(new BorderLayout());
		pnl_quick_power = new JPanel();
		lbl_image = new JLabel(new ImageIcon(img_tree.getImage().getScaledInstance(200, 200, Image.SCALE_DEFAULT)));
		lbl_min = new JLabel("Min. brightness:");
		lbl_max = new JLabel("Max. brightness:");
		lbl_step = new JLabel("Step:");
		lbl_delay = new JLabel("Delay (ms):");
		lbl_mode = new JLabel("Mode:");
		lbl_brightness = new JLabel("Brightness:");
		lbl_perc_bright = new JLabel(BRIGHTNESS_START + " %");
		lbl_bump = new JLabel("Bump by:");
		lbl_status = new JLabel(STATUS_START);
		btn_bump = new JButton("Bump!");
		btn_quick_power_off = new JButton("Quick OFF");
		btn_quick_power_on = new JButton("Quick ON");
		sp_m_min = new SpinnerNumberModel(MIN_START, MIN_MIN, MIN_MAX, SPINNER_STEP);
		sp_min = new JSpinner(sp_m_min);
		sp_m_max = new SpinnerNumberModel(MAX_START, MAX_MIN, MAX_MAX, SPINNER_STEP);
		sp_max = new JSpinner(sp_m_max);
		sp_delay = new JSpinner(new SpinnerNumberModel(DELAY_START, DELAY_MIN, DELAY_MAX, SPINNER_STEP));
		sp_step = new JSpinner(new SpinnerNumberModel(STEP_START, STEP_MIN, STEP_MAX, SPINNER_STEP));
		sp_bump = new JSpinner(new SpinnerNumberModel(BUMP_START, BUMP_MIN, BUMP_MAX, SPINNER_STEP));
		ck_fade = new JCheckBox("Fade");
		sl_brightness = new JSlider(JSlider.HORIZONTAL, 0, 100, BRIGHTNESS_START);
		cmb_mode = new JComboBox<XmasMode>(xmas_modes);
	}
	
	public void setupElements() {
		// Setup elements (for instance dimensions, orientation, ...)
		pnl_controls.setLayout(new BoxLayout(pnl_controls, BoxLayout.Y_AXIS));
		pnl_top.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Basic settings"));
		pnl_bottom.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Advanced settings"));
		lbl_perc_bright.setPreferredSize(new Dimension(50, MIN_MAX_HEIGHT));
		lbl_min.setPreferredSize(new Dimension(MIN_MAX_WIDTH, MIN_MAX_HEIGHT));
		lbl_max.setPreferredSize(new Dimension(MIN_MAX_WIDTH, MIN_MAX_HEIGHT));
		lbl_delay.setPreferredSize(new Dimension(MIN_MAX_WIDTH, MIN_MAX_HEIGHT));
		lbl_step.setPreferredSize(new Dimension(MIN_MAX_WIDTH, MIN_MAX_HEIGHT));
		lbl_status.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		sp_min.setPreferredSize(new Dimension(SPINNER_WIDTH, SPINNER_HEIGHT));
		sp_max.setPreferredSize(new Dimension(SPINNER_WIDTH, SPINNER_HEIGHT));
		sp_step.setPreferredSize(new Dimension(SPINNER_WIDTH, SPINNER_HEIGHT));
		sp_delay.setPreferredSize(new Dimension(SPINNER_WIDTH, SPINNER_HEIGHT));
		sp_bump.setPreferredSize(new Dimension(SPINNER_WIDTH, SPINNER_HEIGHT));
		frame.setEnabled(false);
		frame.setIconImage(img_app_icon.getImage());
	}
	
	public void addElements() {
		// Add elements to their layouts
		pnl_min.add(lbl_min);
		pnl_min.add(sp_min);
		pnl_max.add(lbl_max);
		pnl_max.add(sp_max);
		pnl_delay.add(lbl_delay);
		pnl_delay.add(sp_delay);
		pnl_step.add(lbl_step);
		pnl_step.add(sp_step);
		
		pnl_top.add(lbl_mode);
		pnl_top.add(cmb_mode);
		pnl_top.add(lbl_brightness);
		pnl_top.add(sl_brightness);
		pnl_top.add(lbl_perc_bright);
		pnl_top.add(ck_fade);
		
		pnl_min_max.add(pnl_min);
		pnl_min_max.add(pnl_max);
		pnl_min_max.add(pnl_delay);
		pnl_min_max.add(pnl_step);
		
		pnl_bump.add(lbl_bump);
		pnl_bump.add(sp_bump);
		pnl_bump.add(btn_bump);
		
		pnl_quick_power.add(btn_quick_power_on);
		pnl_quick_power.add(btn_quick_power_off);
		
		pnl_lower_bottom.add(pnl_bump, BorderLayout.LINE_START);
		pnl_lower_bottom.add(new JSeparator(SwingConstants.VERTICAL), BorderLayout.CENTER);
		pnl_lower_bottom.add(pnl_quick_power, BorderLayout.LINE_END);
		
		pnl_bottom.add(pnl_min_max);
		pnl_bottom.add(new JSeparator(SwingConstants.HORIZONTAL));
		pnl_bottom.add(pnl_lower_bottom);
		
		pnl_controls.add(pnl_top);
		pnl_controls.add(pnl_bottom);
		pnl_controls.add(lbl_status);
		
		pnl_image.add(lbl_image);
		
		frame.add(pnl_image);
		frame.add(pnl_controls);
	}
	
	public void addListeners() {
		// Add listeners to elements
		frame.addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent we)
			{
				// Disconnect from MQTT server when window is closed.
				if (client.isConnected()) {
					try {
						client.disconnect();
					} catch (MqttException e) {
						e.printStackTrace();
					}
				}
			}
		});

		cmb_mode.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				// React to a mode change (the user changed the selection of the mode dropdown field)
				if (e.getStateChange() == ItemEvent.SELECTED) {
					toggleStaticControls(((XmasMode) e.getItem()).getAllow_static_setting());
					if(buttons_active) {
						try {
							client.publish(MQTT_TOPIC + "mode", Integer.toString(((XmasMode) e.getItem()).getMode_no()).getBytes(), 0, false);
						} catch (MqttException e1) {
							setStatus("Could not set mode.");
							e1.printStackTrace();
						}
					}
				}
			}
			
		});
		
		sl_brightness.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				// React to a change in the brightness setting
				int new_brightness = ((JSlider) e.getSource()).getValue();
				lbl_perc_bright.setText(new_brightness + " %");
				
				if (buttons_active) {	
					if (!((JSlider) e.getSource()).getValueIsAdjusting()) {
						int scaled_new_brightness = (int) (new_brightness / 100.0 * BRIGHTNESS_MAX);
						setBrightness(scaled_new_brightness);
					}
				}
			}
			
		});
		
		btn_quick_power_on.addActionListener(new ActionListener() {
			// The quick power on button has been pressed
			
			@Override
			public void actionPerformed(ActionEvent e) {
				setBrightness(BRIGHTNESS_MAX);
			}
			
		});
		
		btn_quick_power_off.addActionListener(new ActionListener() {
			// The quick power off button has been pressed
			
			@Override
			public void actionPerformed(ActionEvent e) {
				setBrightness(BRIGHTNESS_MIN);
			}
			
		});
		
		btn_bump.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// Send "bump", ie temporarily increase brightness by a given amount
				try {
					client.publish(MQTT_TOPIC + "bump", sp_bump.getValue().toString().getBytes(), 0, false);
				} catch (MqttException e1) {
					setStatus("Could not send bump.");
					e1.printStackTrace();
				}
			}
			
		});
		
		sp_min.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				// React to a change of the minimum brightness setting
				sp_m_max.setMinimum((int) ((JSpinner) e.getSource()).getValue()+1);
				if (buttons_active) {
					try {
						client.publish(MQTT_TOPIC + "min", ((JSpinner) e.getSource()).getValue().toString().getBytes(), 0, false);
					} catch (MqttException e1) {
						setStatus("Could not set minimum brightness.");
						e1.printStackTrace();
					}
				}
			}
			
		});
	
		sp_max.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				// React to a change of the maximum brightness setting
				sp_m_min.setMaximum((int) ((JSpinner) e.getSource()).getValue()-1);
				if (buttons_active) {
					try {
						client.publish(MQTT_TOPIC + "max", ((JSpinner) e.getSource()).getValue().toString().getBytes(), 0, false);
					} catch (MqttException e1) {
						setStatus("Could not set maximum brightness.");
						e1.printStackTrace();
					}
				}
			}
			
		});
		
		sp_delay.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				// React to a change of the delay setting
				if (buttons_active) {
					try {
						client.publish(MQTT_TOPIC + "delay", ((JSpinner) e.getSource()).getValue().toString().getBytes(), 0, false);
					} catch (MqttException e1) {
						setStatus("Could not set delay.");
						e1.printStackTrace();
					}
				}
			}
			
		});
		
		sp_step.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				// React to a change of the step setting
				if (buttons_active) {
					try {
						client.publish(MQTT_TOPIC + "step", ((JSpinner) e.getSource()).getValue().toString().getBytes(), 0, false);
					} catch (MqttException e1) {
						setStatus("Could not set step.");
						e1.printStackTrace();
					}
				}
			}
			
		});
		
		
	}
	
	public void toggleStaticControls(boolean state) {
		// Enable or disable controls that only make sense when in static mode
		sl_brightness.setEnabled(state);
		ck_fade.setEnabled(state);
		lbl_brightness.setEnabled(state);
		lbl_perc_bright.setEnabled(state);
	}
	
	public void setBrightness(int new_brightness) {
		// Set brightness
		String bright_mode;
		if (ck_fade.getModel().isSelected()) bright_mode = "fade";
		else bright_mode = "brightness";
		
		try {
			client.publish(MQTT_TOPIC + bright_mode, Double.toString(new_brightness).getBytes(), 0, false);
		} catch (MqttException e1) {
			setStatus("Could not set new brightness.");
			e1.printStackTrace();
		}
	}
	
	public void pollStatus() {
		// Poll the Arduino for current status
		try {
			client.publish(MQTT_TOPIC + "get_status", "0".getBytes(), 0, false);
		} catch (MqttException e1) {
			setStatus("Could not poll lights.");
			e1.printStackTrace();
		}
	}
	
	public void parseStatus(String command, int value) {
		// Parse arriving status from Arduino
		buttons_active = false;
		
		float new_bright_float = 0;
		
		switch(command) {
		case "mode":
			System.out.println("Mode change");
			XmasMode _new_mode = (XmasMode) cmb_mode.getSelectedItem();
			for (XmasMode _mode : xmas_modes) {
				if(_mode.getMode_no() == value) {
					_new_mode = _mode;
					break;
				}
			}
			cmb_mode.setSelectedItem(_new_mode);
			break;
		case "brightness":
			new_bright_float = ((float)value/BRIGHTNESS_MAX)*100;
			sl_brightness.setValue((int) new_bright_float);
			ck_fade.setSelected(false);
			break;
		case "fade":
			new_bright_float = ((float)value/BRIGHTNESS_MAX)*100;
			sl_brightness.setValue((int) new_bright_float);
			ck_fade.setSelected(true);
			break;
		case "step":
			sp_step.setValue(value);
			break;
		case "min":
			sp_min.setValue(value);
			break;
		case "max":
			sp_max.setValue(value);
			break;
		case "delay":
			sp_delay.setValue(value);
			break;
		case "bump":
			sp_bump.setValue(value);
			setStatus("Bump received!");
			break;
		default:
			System.out.println("Unknown status received, command: " + command + ", value: " + value);
		}
		
		buttons_active = true;
	}
	
	public void setupMQTT() {
		// Setup our MQTT client. Using SSL is easy from JRE 9 onwards, but difficult for earlier versions.
		try {
			client = new MqttClient( 
				    "wss://" + MQTT_HOST + ":" + MQTT_PORT, //URI 
				    MqttClient.generateClientId(), //ClientId 
				    new MemoryPersistence());
		} catch (MqttException e) {
			setStatus("Error setting up MQTT client.");
			e.printStackTrace();
		}
		
		mqtt_options = new MqttConnectOptions();
		mqtt_options.setUserName(MQTT_USER);
		mqtt_options.setPassword(MQTT_PASS.toCharArray());
		mqtt_options.setAutomaticReconnect(true);
		
		client.setCallback(new MqttCallback() {

			@Override
			public void connectionLost(Throwable cause) {
				setStatus("Connection to MQTT broker lost. Reconnecting ...");
				cause.printStackTrace();
			}

			@Override
			public void messageArrived(String topic, MqttMessage message) throws Exception {
				System.out.println("Message arrived " + topic + "  " + new String(message.getPayload()));
				if (topic.contains(MQTT_STATUS)) {
					System.out.println("To mode parser");
					String clean_topic = topic.substring(topic.lastIndexOf("/") + 1);
					int clean_int = 0;
					try {
						clean_int = Integer.parseInt(new String(message.getPayload()));
					}
					catch (Exception e) {
						System.out.println("Could not convert status value to integer.");
					}
					parseStatus(clean_topic, clean_int);
				}
			}

			@Override
			public void deliveryComplete(IMqttDeliveryToken token) {
				// TODO Auto-generated method stub
				
			}
			
		});

		SwingWorker<Boolean, Void> connection_worker = new SwingWorker<Boolean, Void>() {
			@Override
			protected Boolean doInBackground() throws Exception {
				try {
					client.connect(mqtt_options);
					client.subscribe(MQTT_STATUS + "#");
				} catch (MqttException e) {
					e.printStackTrace();
					return false;
				}
				return true;
			}

			protected void done() {
				boolean status;
				try {
					// Retrieve the return value of doInBackground.
					status = get();
					if(status) {
						setStatus("Connected to MQTT broker " + MQTT_HOST + ".");
						pollStatus();
						frame.setEnabled(true);
					}
					else setStatus("Error connecting to MQTT broker.");
				} catch (InterruptedException e) {
					// This is thrown if the thread's interrupted.
					setStatus("MQTT connection thread interrupted.");
				} catch (ExecutionException e) {
					// This is thrown if we throw an exception
					// from doInBackground.
					setStatus("Error connecting to MQTT broker.");
				}
			}
		};

		connection_worker.execute();
	}
	
	public void setStatus(String new_status) {
		// Prints given string to the status bar of the GUI
		String timestamp = new SimpleDateFormat("HH.mm").format(new Date());
		lbl_status.setText("[" + timestamp + "] " + new_status);
	}
	
	public void setLAF() {
		// Sets the platform's native look and feel
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				new XmasRemote().createGUI();
			}
		});
	}

}
