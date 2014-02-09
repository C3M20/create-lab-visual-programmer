package edu.cmu.ri.createlab.visualprogrammer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.io.File;
import java.util.List;
import java.util.PropertyResourceBundle;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import edu.cmu.ri.createlab.device.CreateLabDevicePingFailureEventListener;
import edu.cmu.ri.createlab.device.CreateLabDeviceProxy;
import edu.cmu.ri.createlab.expressionbuilder.ExpressionBuilder;
import edu.cmu.ri.createlab.sequencebuilder.SequenceBuilder;
import edu.cmu.ri.createlab.sequencebuilder.SequenceExecutor;
import edu.cmu.ri.createlab.terk.services.ServiceManager;
import edu.cmu.ri.createlab.userinterface.util.DialogHelper;
import edu.cmu.ri.createlab.userinterface.util.ImageUtils;
import edu.cmu.ri.createlab.util.StandardVersionNumber;
import edu.cmu.ri.createlab.visualprogrammer.lookandfeel.VisualProgrammerLookAndFeelLoader;
import edu.cmu.ri.createlab.xml.LocalEntityResolver;
import edu.cmu.ri.createlab.xml.XmlHelper;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class VisualProgrammer
   {
   private static final Logger LOG = Logger.getLogger(VisualProgrammer.class);

   private static final PropertyResourceBundle RESOURCES = (PropertyResourceBundle)PropertyResourceBundle.getBundle(VisualProgrammer.class.getName());

   private static final String APPLICATION_NAME = RESOURCES.getString("application.name");
   private static final StandardVersionNumber VERSION_NUMBER = StandardVersionNumber.parse(RESOURCES.getString("version.number"));
   private static final String APPLICATION_NAME_AND_VERSION_NUMBER = APPLICATION_NAME + " v" + VERSION_NUMBER;
   private final JFrame jFrame;

   public interface TabSwitcher
      {
      /** Switches to the Expression Builder tab. This method assumes it's being called from the Swing thread. */
      void showExpressionBuilderTab();

      /** Switches to the Sequence Builder tab. This method assumes it's being called from the Swing thread. */
      void showSequenceBuilderTab();

      /** Switches to the Sequence Builder tab. This method assumes it's being called from the Swing thread. */
      void showSettingsTab();
      }

   public static void main(final String[] args)
      {
      // Load the look and feel
      VisualProgrammerLookAndFeelLoader.getInstance().loadLookAndFeel();

      //Schedule a job for the event-dispatching thread: creating and showing this application's GUI.
      SwingUtilities.invokeLater(
            new Runnable()
            {
            public void run()
               {
               final JFrame theJFrame = new JFrame(APPLICATION_NAME_AND_VERSION_NUMBER);

               final VisualProgrammer application = new VisualProgrammer(theJFrame);

               // set various properties for the JFrame
               theJFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
               theJFrame.setBackground(Color.WHITE);
               theJFrame.setPreferredSize(new Dimension(1024, 728));
               theJFrame.setResizable(true);
               theJFrame.addWindowListener(
                     new WindowAdapter()
                     {
                     public void windowClosing(final WindowEvent event)
                        {
                        // ask if the user really wants to exit
                        final int selectedOption = JOptionPane.showConfirmDialog(theJFrame,
                                                                                 RESOURCES.getString("dialog.message.exit-confirmation"),
                                                                                 RESOURCES.getString("dialog.title.exit-confirmation"),
                                                                                 JOptionPane.YES_NO_OPTION,
                                                                                 JOptionPane.QUESTION_MESSAGE);

                        if (selectedOption == JOptionPane.YES_OPTION)
                           {
                           final SwingWorker<Object, Object> worker =
                                 new SwingWorker<Object, Object>()
                                 {
                                 @Override
                                 protected Object doInBackground() throws Exception
                                    {
                                    application.shutdown();
                                    return null;
                                    }

                                 @Override
                                 protected void done()
                                    {
                                    System.exit(0);
                                    }
                                 };
                           worker.execute();
                           }
                        }
                     });

               theJFrame.addWindowStateListener(new WindowStateListener()
               {
               @Override
               public void windowStateChanged(final WindowEvent e)
                  {
                  //To change body of implemented methods use File | Settings | File Templates.

                  final int state = e.getNewState();
                  String strState = " ";

                  if ((state == Frame.NORMAL))
                     {
                     strState += "NORMAL ";
                     }
                  if ((state & Frame.ICONIFIED) != 0)
                     {
                     strState += "ICONIFIED ";
                     }
                  // MAXIMIZED_BOTH is a concatenation of two bits, so
                  // we need to test for an exact match.
                  if ((state & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH)
                     {
                     strState += "MAXIMIZED_BOTH ";
                     }
                  else
                     {
                     if ((state & Frame.MAXIMIZED_VERT) != 0)
                        {
                        strState += "MAXIMIZED_VERT";
                        }
                     if ((state & Frame.MAXIMIZED_HORIZ) != 0)
                        {
                        strState += "MAXIMIZED_HORIZ";
                        }
                     }
                  if (" ".equals(strState))
                     {
                     strState = "UNKNOWN";
                     }
                  LOG.debug("Window State Changed: " + strState);
                  theJFrame.setPreferredSize(((JFrame)e.getSource()).getSize());
                  theJFrame.pack();
                  theJFrame.repaint();
                  }
               });

               theJFrame.addWindowFocusListener(
                     new WindowAdapter()
                     {
                     public void windowGainedFocus(final WindowEvent e)
                        {
                        theJFrame.repaint();
                        }
                     });

               theJFrame.addComponentListener(
                     new ComponentAdapter()
                     {
                     @Override
                     public void componentResized(final ComponentEvent e)
                        {
                        final Component source = e.getComponent();
                        final Dimension size = source.getSize();
                        LOG.debug("Window State Changed: RESIZED");
                        theJFrame.setPreferredSize(size);
                        theJFrame.pack();
                        theJFrame.repaint();
                        }
                     });

               theJFrame.pack();
               theJFrame.setLocationRelativeTo(null);    // center the window on the screen
               theJFrame.setVisible(true);
               }
            });
      }

   private ServiceManager serviceManager = null;
   private CreateLabDeviceProxy createLabDeviceProxy = null;
   private final VisualProgrammerDeviceImplementationClassLoader visualProgrammerDeviceImplementationClassLoader = new VisualProgrammerDeviceImplementationClassLoader();

   private ExpressionBuilder expressionBuilder = null;
   private SequenceBuilder sequenceBuilder = null;

   private final JPanel mainPanel = new JPanel();
   private final JTabbedPane tabbedPane = new JTabbedPane();
   private JLabel hintsGraphic = null;
   private JLabel connectingGraphic = null;

   private VisualProgrammer(@NotNull final JFrame jFrame)
      {
      this.jFrame = jFrame;

      XmlHelper.setLocalEntityResolver(LocalEntityResolver.getInstance());

      // connect to the device...
      connectToDevice();
      }

   private void connectToDevice()
      {
      if (isConnected())
         {
         LOG.info("VisualProgrammer.connectToDevice(): doing nothing since we're already connected.");
         }
      else
         {
         // connect to the device...
         final SwingWorker<VisualProgrammerDevice, Object> sw =
               new SwingWorker<VisualProgrammerDevice, Object>()
               {
               @Override
               protected VisualProgrammerDevice doInBackground() throws Exception
                  {
                  LOG.debug("VisualProgrammer.connectToDevice(): connecting to device...");

                  // first get the class names
                  final List<VisualProgrammerDevice> visualProgrammerDevices = visualProgrammerDeviceImplementationClassLoader.loadImplementationClasses();

                  if (visualProgrammerDevices.size() > 0)
                     {
                     // TODO: present the user with a choice.  For now, just take the first one...
                     final VisualProgrammerDevice visualProgrammerDevice = visualProgrammerDevices.get(0);

                     // Configure the main panel
                     connectingGraphic = new JLabel(visualProgrammerDevice.getConnectingImage());
                     hintsGraphic = new JLabel(visualProgrammerDevice.getConnectionTipsImage());

                     //Add decorative border to hintsGraphic
                     final Border purple = BorderFactory.createMatteBorder(4, 4, 4, 4, new Color(197, 193, 235));
                     hintsGraphic.setBorder(purple);

                     showSpinner();

                     // connect to the device...
                     visualProgrammerDevice.connect();
                     LOG.debug("VisualProgrammer.connectToDevice(): Connected!");

                     createLabDeviceProxy = visualProgrammerDevice.getDeviceProxy();
                     serviceManager = visualProgrammerDevice.getServiceManager();

                     createLabDeviceProxy.addCreateLabDevicePingFailureEventListener(
                           new CreateLabDevicePingFailureEventListener()
                           {
                           @Override
                           public void handlePingFailureEvent()
                              {
                              LOG.debug("VisualProgrammer.handlePingFailureEvent(): cleaning up after ping failure...");
                              cleanup(false);

                              LOG.debug("VisualProgrammer.handlePingFailureEvent(): attempting reconnection to device...");
                              connectToDevice();
                              }
                           });

                     return visualProgrammerDevice;
                     }
                  else
                     {
                     // TODO: alert the user before shutting down
                     LOG.error("Could not find any valid implementations of class VisualProgrammerDevice.  Will now exit.");
                     System.exit(1);
                     }

                  return null;
                  }

               @Override
               protected void done()
                  {
                  try
                     {
                     final VisualProgrammerDevice visualProgrammerDevice = get();

                     // Check preferences to see whether the home directory is already defined and should be used
                     File preferredHomeDirectory = null;
                     if (UserPreferences.isBackingStoreAvailable())
                        {
                        if (UserPreferences.getInstance().hasPreferences())
                           {
                           if (UserPreferences.getInstance().shouldRememberHomeDirectory())
                              {
                              preferredHomeDirectory = UserPreferences.getInstance().getHomeDirectory();
                              if (!PathManager.getInstance().isValidDirectory(preferredHomeDirectory))
                                 {
                                 preferredHomeDirectory = null;

                                 // directory is no longer valid (e.g. drive ejected, or a network drive), so wipe the prefs
                                 UserPreferences.getInstance().setHomeDirectory(null);
                                 UserPreferences.getInstance().setShouldRememberHomeDirectory(false);
                                 }
                              }
                           }
                        else
                           {
                           UserPreferences.getInstance().initializePreferences();
                           }
                        }

                     final HomeDirectoryChooserEventHandler homeDirectoryChooserEventHandler = new HomeDirectoryChooserEventHandler(visualProgrammerDevice);
                     if (preferredHomeDirectory == null)
                        {
                        mainPanel.removeAll();
                        mainPanel.add(HomeDirectoryChooser.getInstance().createChooserPanelForStartup(jFrame, homeDirectoryChooserEventHandler));
                        jFrame.pack();
                        jFrame.repaint();
                        }
                     else
                        {
                        homeDirectoryChooserEventHandler.onDirectoryChosen(preferredHomeDirectory);
                        }
                     }
                  catch (final InterruptedException e)
                     {
                     LOG.error("InterruptedException while trying to get the visualProgrammerDevice", e);
                     }
                  catch (final ExecutionException e)
                     {
                     LOG.error("ExecutionException while trying to get the visualProgrammerDevice", e);
                     }
                  }
               };
         sw.execute();
         }
      }

   private void cleanup(final boolean willAttemptDisconnect)
      {
      if (isConnected())
         {
         try
            {
            if (willAttemptDisconnect)
               {
               LOG.debug("VisualProgrammer.cleanup(): disconnecting from device...");
               createLabDeviceProxy.disconnect();
               LOG.debug("VisualProgrammer.cleanup(): disconnected!");
               }
            }
         catch (final Exception e)
            {
            LOG.error("Exception while trying to disconnect from the device.  Ignoring.", e);
            }
         finally
            {
            LOG.debug("VisualProgrammer.cleanup(): cleaning up...");

            createLabDeviceProxy = null;
            serviceManager = null;

            PathManager.getInstance().deinitialize();

            expressionBuilder.performPostDisconnectCleanup();
            //TODO: sequenceBuilder.performPostDisconnectCleanup();

            expressionBuilder = null;
            sequenceBuilder = null;

            showSpinner();
            }
         }
      else
         {
         LOG.info("VisualProgrammer.cleanup(): doing nothing since we're already disconnected.");
         }
      }

   private boolean isConnected()
      {
      return serviceManager != null && createLabDeviceProxy != null;
      }

   private void shutdown()
      {
      LOG.debug("VisualProgrammer.shutdown()");

      if (SequenceExecutor.getInstance().isRunning())
         {
         LOG.debug("VisualProgrammer.shutdown(): stopping sequence playback");
         SequenceExecutor.getInstance().stop();
         }

      LOG.debug("VisualProgrammer.shutdown(): disconnecting from device");
      cleanup(true);
      }

   private void showSpinner()
      {
      mainPanel.removeAll();

      mainPanel.setLayout(new GridBagLayout());

      final GridBagConstraints gbc = new GridBagConstraints();
      gbc.fill = GridBagConstraints.NONE;
      gbc.gridx = 0;
      gbc.gridy = 0;
      gbc.weighty = 0.5;
      gbc.weightx = 1.0;
      gbc.anchor = GridBagConstraints.PAGE_END;
      gbc.insets = new Insets(0, 0, 20, 0);
      mainPanel.add(connectingGraphic, gbc);

      gbc.fill = GridBagConstraints.NONE;
      gbc.gridx = 0;
      gbc.gridy = 1;
      gbc.weighty = 0.0;
      gbc.weightx = 1.0;
      gbc.insets = new Insets(0, 0, 0, 0);
      gbc.anchor = GridBagConstraints.PAGE_START;
      mainPanel.add(hintsGraphic, gbc);

      final JLabel versionLabel = new JLabel(APPLICATION_NAME_AND_VERSION_NUMBER);

      gbc.fill = GridBagConstraints.NONE;
      gbc.gridx = 0;
      gbc.gridy = 2;
      gbc.weighty = 0.5;
      gbc.weightx = 1.0;
      gbc.anchor = GridBagConstraints.LAST_LINE_START;
      mainPanel.add(versionLabel, gbc);

      jFrame.setLayout(new GridBagLayout());

      gbc.fill = GridBagConstraints.BOTH;
      gbc.gridx = 0;
      gbc.gridy = 0;
      gbc.weighty = 1.0;
      gbc.weightx = 1.0;
      gbc.anchor = GridBagConstraints.CENTER;
      jFrame.add(mainPanel, gbc);

      mainPanel.setName("mainPanel");
      versionLabel.setName("versionNumber");

      jFrame.pack();
      jFrame.repaint();
      jFrame.setLocationRelativeTo(null);
      }

   private class HomeDirectoryChooserEventHandler implements HomeDirectoryChooser.EventHandler
      {
      private final VisualProgrammerDevice visualProgrammerDevice;

      private HomeDirectoryChooserEventHandler(final VisualProgrammerDevice visualProgrammerDevice)
         {
         this.visualProgrammerDevice = visualProgrammerDevice;
         }

      @Override
      public void onDirectoryChosen(@NotNull final File homeDirectory)
         {
         LOG.info("Starting VisualProgrammerDevice");

         // Now that we know the home directory, we can proceed
         PathManager.getInstance().initialize(homeDirectory, visualProgrammerDevice);

         // IF AND ONLY IF the home directory is the same as the default directory, then look for the existence
         // of the Audio directory in the old location.  If it's there, and the one in the NEW location exists and
         // is empty, then copy the contents of the old to the new.
         //
         // NOTE: We do this BEFORE we create the ExpressionBuilder, because the ExpressionBuilder does the audio
         // file installation, but the installer won't overwrite files that already exist.
         try
            {
            if (VisualProgrammerConstants.FilePaths.DEFAULT_VISUAL_PROGRAMMER_HOME_DIR.equals(homeDirectory))
               {
               if (VisualProgrammerConstants.FilePaths.FORMER_AUDIO_DIR.isDirectory())
                  {
                  if (PathManager.getInstance().getAudioDirectory().isDirectory())
                     {
                     final File[] files = PathManager.getInstance().getAudioDirectory().listFiles();
                     if (files != null && files.length <= 0)
                        {
                        LOG.info("VisualProgrammer.HomeDirectoryChooserEventHandler.onDirectoryChosen(): Copying audio files from the old location to the new!");
                        FileUtils.copyDirectory(VisualProgrammerConstants.FilePaths.FORMER_AUDIO_DIR,
                                                PathManager.getInstance().getAudioDirectory());
                        }
                     }
                  }
               }
            }
         catch (final Exception e)
            {
            LOG.error("Exception while trying to copy the files in the Audio directory from the old location to the new", e);
            }

         expressionBuilder = new ExpressionBuilder(jFrame, visualProgrammerDevice,
                                                   new TabSwitcher()
                                                   {
                                                   @Override
                                                   public void showExpressionBuilderTab()
                                                      {
                                                      tabbedPane.setSelectedIndex(0);
                                                      }

                                                   @Override
                                                   public void showSequenceBuilderTab()
                                                      {
                                                      tabbedPane.setSelectedIndex(1);
                                                      }

                                                   @Override
                                                   public void showSettingsTab()
                                                      {
                                                      tabbedPane.setSelectedIndex(2);
                                                      }
                                                   });
         sequenceBuilder = new SequenceBuilder(jFrame, visualProgrammerDevice, expressionBuilder);

         final UpdateChecker updateChecker = new UpdateChecker(VERSION_NUMBER);
         updateChecker.addUpdateCheckResultListener(
               new UpdateChecker.UpdateCheckResultListener()
               {
               @Override
               public void handleUpdateCheckResult(final boolean wasCheckSuccessful,
                                                   final boolean isUpdateAvailable,
                                                   @Nullable final StandardVersionNumber versionNumberOfUpdate)
                  {
                  // Make sure this happens in the Swing thread...
                  SwingUtilities.invokeLater(
                        new Runnable()
                        {
                        @Override
                        public void run()
                           {
                           if (wasCheckSuccessful && isUpdateAvailable)
                              {
                              tabbedPane.setTitleAt(2, RESOURCES.getString("settings-tab.name") + " ");
                              }
                           }
                        });
                  }
               });
         final SettingsPanel settingsPanel = new SettingsPanel(jFrame, VERSION_NUMBER, updateChecker);

         // clear everything out of the mainPanel and recreate it
         mainPanel.removeAll();
         tabbedPane.removeAll();
         tabbedPane.setFocusable(false);
         tabbedPane.addTab(RESOURCES.getString("expression-builder-tab.name"), expressionBuilder.getPanel());
         tabbedPane.addTab(RESOURCES.getString("sequence-builder-tab.name"), sequenceBuilder.getPanel());
         tabbedPane.addTab(null, ImageUtils.createImageIcon(RESOURCES.getString("settings-tab.icon")), settingsPanel.getPanel());
         tabbedPane.setFont(new Font("Verdana", Font.PLAIN, 11));

         tabbedPane.setMnemonicAt(0, KeyEvent.VK_E);
         tabbedPane.setMnemonicAt(1, KeyEvent.VK_Q);
         tabbedPane.setMnemonicAt(2, KeyEvent.VK_COMMA);

         tabbedPane.addChangeListener(
               new ChangeListener()
               {
               @Override
               public void stateChanged(final ChangeEvent changeEvent)
                  {
                  if (tabbedPane.getSelectedIndex() != 1)
                     {
                     // If a sequence is playing, then ask the user whether she wants to stop it
                     // now that the Sequence Builder tab is no longer visible
                     if (SequenceExecutor.getInstance().isRunning())
                        {
                        if (DialogHelper.showYesNoDialog(RESOURCES.getString("dialog.title.confirm-stop-sequence-playback-when-sequence-builder-tab-loses-focus"),
                                                         RESOURCES.getString("dialog.message.confirm-stop-sequence-playback-when-sequence-builder-tab-loses-focus"),
                                                         jFrame))
                           {
                           SequenceExecutor.getInstance().stop();
                           }
                        }
                     }
                  }
               }
         );
         jFrame.setPreferredSize(new Dimension(1024, 728));

         mainPanel.setLayout(new GridBagLayout());

         final GridBagConstraints c = new GridBagConstraints();
         c.fill = GridBagConstraints.BOTH;
         c.gridx = 0;
         c.gridy = 0;
         c.weighty = 1.0;
         c.weightx = 1.0;
         c.anchor = GridBagConstraints.CENTER;
         mainPanel.add(tabbedPane, c);

         jFrame.pack();
         jFrame.repaint();
         jFrame.setLocationRelativeTo(null);    // center the window on the screen

         // now that the tabs have been created and added to the JFrame, we can initiate the update check
         updateChecker.checkForUpdate();
         }
      }
   }
