package edu.cmu.ri.createlab.sequencebuilder;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.PropertyResourceBundle;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.ExpressionModel;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.ProgramElementModel;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.SavedSequenceModel;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.ProgramElementView;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.listcell.ExpressionListCellView;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.listcell.SavedSequenceListCellView;

import edu.cmu.ri.createlab.sequencebuilder.programelement.view.standard.StandardViewFactory;
import edu.cmu.ri.createlab.userinterface.GUIConstants;
import edu.cmu.ri.createlab.userinterface.util.DialogHelper;
import edu.cmu.ri.createlab.userinterface.util.ImageUtils;
import edu.cmu.ri.createlab.userinterface.util.SwingUtils;
import edu.cmu.ri.createlab.util.AbstractDirectoryPollingListModel;
import edu.cmu.ri.createlab.visualprogrammer.VisualProgrammerDevice;
import org.jetbrains.annotations.NotNull;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
final class FileManagerControlsView
   {
   private static final PropertyResourceBundle RESOURCES = (PropertyResourceBundle)PropertyResourceBundle.getBundle(FileManagerControlsView.class.getName());

   private final JPanel panel = new JPanel();

   private final JButton appendButton = SwingUtils.createButton(RESOURCES.getString("button.label.append"));
   private final JButton openButton;
   private final JButton deleteButton = SwingUtils.createButton(RESOURCES.getString("button.label.delete_exp"));

   private final JFrame jFrame;
   private final JList expressionSourceList;
   private final JList savedSequenceSourceList;

   private final Sequence sequence;
   private final FileManagerControlsController fileManagerControlsController;
   private final VisualProgrammerDevice visualProgrammerDevice;

   FileManagerControlsView(final JFrame jFrame,
                           final Sequence seq,
                           final JButton open,
                           final JList expressionSourceList,
                           final JList savedSequenceSourceList,
                           final VisualProgrammerDevice visualProgrammerD,
                           final FileManagerControlsController fileManagerCC)
      {
      this.jFrame = jFrame;
      this.expressionSourceList = expressionSourceList;
      this.savedSequenceSourceList = savedSequenceSourceList;
      this.openButton = open;
      this.sequence = seq;
      this.fileManagerControlsController = fileManagerCC;
      this.visualProgrammerDevice = visualProgrammerD;

       deleteButton.setIcon(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/sequencebuilder/images/deleteMark.png"));

      final GroupLayout layout = new GroupLayout(panel);
      panel.setLayout(new GridBagLayout());
      panel.setBackground(Color.WHITE);

      GridBagConstraints gbc = new GridBagConstraints();

      gbc.fill = GridBagConstraints.HORIZONTAL;
      gbc.gridx = 0;
      gbc.gridy = 0;
      gbc.weighty = 0.0;
      gbc.weightx = 1.0;
      gbc.anchor = GridBagConstraints.PAGE_END;

      panel.add(deleteButton, gbc);

      // add selection listeners which allow us to toggle whether the buttons are enabled
      expressionSourceList.addListSelectionListener(
            new ListSelectionListener()
            {
            @Override
            public void valueChanged(final ListSelectionEvent listSelectionEvent)
               {
               toggleButtons();
               }
            }
      );
      savedSequenceSourceList.addListSelectionListener(
            new ListSelectionListener()
            {
            @Override
            public void valueChanged(final ListSelectionEvent listSelectionEvent)
               {
               toggleButtons();
               }
            }
      );

      appendButton.addActionListener(
            new ActionListener()
            {
            @Override
            public void actionPerformed(final ActionEvent actionEvent)
               {
               ProgramElementView view = null;
               if (!expressionSourceList.isSelectionEmpty())
                  {
                  view = (ExpressionListCellView)expressionSourceList.getSelectedValue();
                  }
               else if (!savedSequenceSourceList.isSelectionEmpty())
                  {
                  view = (SavedSequenceListCellView)savedSequenceSourceList.getSelectedValue();
                  }

               if (view != null)
                  {
                  final ProgramElementModel model = view.getProgramElementModel().createCopy();
                  final SwingWorker sw =
                        new SwingWorker<Object, Object>()
                        {
                        @Override
                        protected Object doInBackground() throws Exception
                           {
                           sequence.appendProgramElement(model);
                           return null;
                           }
                        };
                  sw.execute();
                  }
               }
            }
      );


       ActionListener openAction = new ActionListener()
            {
            @Override
            public void actionPerformed(final ActionEvent actionEvent)
               {

               final FileListDialogPanel listPanel = new FileListDialogPanel();
               savedSequenceSourceList.clearSelection();
               final int selection = JOptionPane.showConfirmDialog(jFrame,
                                                       listPanel,
                                                       "Open",
                                                       JOptionPane.OK_CANCEL_OPTION,
                                                       JOptionPane.PLAIN_MESSAGE);

               if (selection==JOptionPane.OK_OPTION){
               final int selectedIndex = listPanel.getResults();
               if (selectedIndex >= 0)
                {
                      final SavedSequenceListCellView savedSequenceListCellView = listPanel.getValue();
                      final SavedSequenceModel savedSequenceModel = savedSequenceListCellView.getProgramElementModel();
                      final String message = MessageFormat.format(RESOURCES.getString("dialog.message.open-sequence-confirmation"), savedSequenceModel.getName());
                      //TODO: Something has broken and this doesn't properly open sequences (probably caused by having multiple models)
                      if (sequence.isEmpty() || DialogHelper.showYesNoDialog(RESOURCES.getString("dialog.title.open-sequence-confirmation"), message, jFrame))
                         {
                         jFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                         final SwingWorker sw =
                               new SwingWorker<Object, Object>()
                               {
                               @Override
                               protected Object doInBackground() throws Exception
                                  {
                                  fileManagerControlsController.openSequence(savedSequenceModel);
                                  return null;
                                  }

                               @Override
                               protected void done()
                                  {
                                  jFrame.setCursor(Cursor.getDefaultCursor());
                                  }
                               };
                         sw.execute();
                         }
                      }
               }
               else
               {
               JOptionPane.showMessageDialog(jFrame,
                                               "Please select an Sequence to open from the provided list.",
                                               "Open Error",
                                               JOptionPane.WARNING_MESSAGE);
               actionPerformed(actionEvent);
               }
            }
       };


      openButton.addActionListener(openAction);


      deleteButton.addActionListener(
            new ActionListener()
            {
            @Override
            public void actionPerformed(final ActionEvent actionEvent)
               {
               FileDeleter fileDeleter = null;
               if (!expressionSourceList.isSelectionEmpty())
                  {
                  final ExpressionListCellView expressionListCellView = (ExpressionListCellView)expressionSourceList.getSelectedValue();
                  final ExpressionModel expressionModel = expressionListCellView.getProgramElementModel();
                  fileDeleter =
                        new FileDeleter<ExpressionModel>(expressionModel,
                                                         expressionSourceList,
                                                         RESOURCES.getString("dialog.message.delete-expression-confirmation"))
                        {
                        @Override
                        protected void performDelete(final ExpressionModel model)
                           {
                           fileManagerControlsController.deleteExpression(model);
                           }
                        };
                  }
               else if (!savedSequenceSourceList.isSelectionEmpty())
                  {
                  final SavedSequenceListCellView savedSequenceListCellView = (SavedSequenceListCellView)savedSequenceSourceList.getSelectedValue();
                  final SavedSequenceModel savedSequenceModel = savedSequenceListCellView.getProgramElementModel();
                  fileDeleter =
                        new FileDeleter<SavedSequenceModel>(savedSequenceModel,
                                                            savedSequenceSourceList,
                                                            RESOURCES.getString("dialog.message.delete-sequence-confirmation"))
                        {
                        @Override
                        protected void performDelete(final SavedSequenceModel model)
                           {
                           fileManagerControlsController.deleteSequence(model);
                           }
                        };
                  }

               if (fileDeleter != null)
                  {
                  fileDeleter.delete();
                  }
               }
            }
      );
      }

   private void toggleButtons()
      {
      if (!savedSequenceSourceList.isSelectionEmpty())
         {
         openButton.setEnabled(true);
         }
      else
         {
         openButton.setEnabled(false);
         }
      final boolean isSomethingSelected = !savedSequenceSourceList.isSelectionEmpty() || !expressionSourceList.isSelectionEmpty();
      appendButton.setEnabled(isSomethingSelected);

      if(!savedSequenceSourceList.isSelectionEmpty())
      {
      deleteButton.setText(RESOURCES.getString("button.label.delete_seq"));
      }
      else if (!expressionSourceList.isSelectionEmpty())
      {
      deleteButton.setText(RESOURCES.getString("button.label.delete_exp"));
      }

      deleteButton.setEnabled(isSomethingSelected);
      }

   JComponent getComponent()
      {
      return panel;
      }

   void doClickOnAppendExpressionOrOpenSequenceButton()
      {
      if (!expressionSourceList.isSelectionEmpty())
                  {
                  //final ProgramElementView view = (ExpressionListCellView)expressionSourceList.getSelectedValue();
                  //final ProgramElementModel expressionModel = view.getProgramElementModel().createCopy();
                  // TODO: open the expression in Expression Builder (if not running Sequence Builder as standalone app)
                  }
               else if (!savedSequenceSourceList.isSelectionEmpty())
                  {
                  final SavedSequenceListCellView savedSequenceListCellView = (SavedSequenceListCellView)savedSequenceSourceList.getSelectedValue();
                  final SavedSequenceModel savedSequenceModel = savedSequenceListCellView.getProgramElementModel();
                  final String message = MessageFormat.format(RESOURCES.getString("dialog.message.open-sequence-confirmation"), savedSequenceModel.getName());
                  if (sequence.isEmpty() || DialogHelper.showYesNoDialog(RESOURCES.getString("dialog.title.open-sequence-confirmation"), message, jFrame))
                     {
                     jFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                     final SwingWorker sw =
                           new SwingWorker<Object, Object>()
                           {
                           @Override
                           protected Object doInBackground() throws Exception
                              {
                              fileManagerControlsController.openSequence(savedSequenceModel);
                              return null;
                              }

                           @Override
                           protected void done()
                              {
                              jFrame.setCursor(Cursor.getDefaultCursor());
                              }
                           };
                     sw.execute();
                     }
                  }
      }

   private abstract class FileDeleter<ModelClass extends ProgramElementModel>
      {
      private final ModelClass model;
      private final JList jList;
      private final String confirmationMessage;

      private FileDeleter(@NotNull final ModelClass model,
                          @NotNull final JList jList,
                          @NotNull final String confirmationMessage)
         {
         this.model = model;
         this.jList = jList;
         this.confirmationMessage = confirmationMessage;
         }

      public void delete()
         {

         final String message = MessageFormat.format(confirmationMessage, model.getName());
         final int selectedOption = JOptionPane.showConfirmDialog(jFrame,
                                                                  message,
                                                                  RESOURCES.getString("dialog.title.delete-confirmation"),
                                                                  JOptionPane.YES_NO_OPTION,
                                                                  JOptionPane.WARNING_MESSAGE);

         if (selectedOption == JOptionPane.YES_OPTION)
            {
            final SwingWorker sw =
                  new SwingWorker<Object, Object>()
                  {
                  @Override
                  protected Object doInBackground() throws Exception
                     {
                     performDelete(model);
                     return null;
                     }

                  @Override
                  protected void done()
                     {
                     jList.repaint();
                     super.done();
                     }
                  };
            sw.execute();
            }
         }

      protected abstract void performDelete(final ModelClass model);
      }

     private class FileListDialogPanel extends JPanel{

         private final GridBagConstraints gbc = new GridBagConstraints();
         private final GridBagLayout gbl = new GridBagLayout();
         private final ArrayList options = new ArrayList();

         private final ContainerView sequenceContainerView = new ContainerView(jFrame, new ContainerModel(), new StandardViewFactory());
         private final SavedSequenceFileListModel savedSequenceSourceListModel = new SavedSequenceFileListModel(sequenceContainerView);
         private final JList savedSequenceList = new JList(savedSequenceSourceListModel);
         private final JScrollPane savedSequenceSourceListScrollPane = new JScrollPane(savedSequenceSourceList);

        FileListDialogPanel(){
             super();
             this.setLayout(gbl);

             savedSequenceSourceListScrollPane.setBorder(BorderFactory.createLineBorder(Color.gray));

             gbc.fill = GridBagConstraints.HORIZONTAL;
             gbc.gridx = 0;
             gbc.gridy = 0;
             gbc.weighty = 0.0;
             gbc.weightx = 1.0;
             gbc.insets = new Insets(2,2,2,2);
             gbc.anchor = GridBagConstraints.LINE_START;

             this.add(SwingUtils.createLabel("Select a Sequence to open:"));

             gbc.gridy = 1;
             gbc.weighty = 1.0;
             gbc.fill = GridBagConstraints.BOTH;

             this.add(savedSequenceSourceListScrollPane,gbc);

             this.setMinimumSize(new Dimension(180, 300));
             this.setPreferredSize(new Dimension(180, 300));
         }

         public int getResults(){
           if  (savedSequenceList.isSelectionEmpty()){
           return -1;
           }
           else {
           return savedSequenceList.getSelectedIndex();
           }
         }

          public SavedSequenceListCellView getValue(){
             return (SavedSequenceListCellView)savedSequenceList.getSelectedValue();
          }

         public void addComponent(Component component,int xpos, int ypos){
           gbc.gridx = xpos;
           gbc.gridy = ypos;
           gbc.insets = new Insets(2,2,2,2);
           gbl.setConstraints(component,gbc);
           this.add(component);
         }

         public void addComponent (Component component,int xpos,int ypos,int anchor){
           gbc.anchor = anchor;
           addComponent(component,xpos,ypos);
         }

    }

     private final class SavedSequenceFileListModel extends AbstractDirectoryPollingListModel<SavedSequenceListCellView>
      {
      private final ContainerView containerView;

      private SavedSequenceFileListModel(@NotNull final ContainerView containerView)
         {
         super(
               new Comparator<SavedSequenceListCellView>()
               {
               @Override
               public int compare(final SavedSequenceListCellView view1, final SavedSequenceListCellView view2)
                  {
                  return view1.getProgramElementModel().getSavedSequenceFile().compareTo(view2.getProgramElementModel().getSavedSequenceFile());
                  }
               });
         this.containerView = containerView;
         }

      @Override
      protected SavedSequenceListCellView createListItemInstance(@NotNull final File file)
         {
         return new SavedSequenceListCellView(containerView, new SavedSequenceModel(visualProgrammerDevice, file));
         }
      }

   }
