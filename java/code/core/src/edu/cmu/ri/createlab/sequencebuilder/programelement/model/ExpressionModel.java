package edu.cmu.ri.createlab.sequencebuilder.programelement.model;

import java.io.File;
import java.io.IOException;
import edu.cmu.ri.createlab.terk.expression.XmlExpression;
import org.apache.log4j.Logger;
import org.jdom.JDOMException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * <p>
 * <code>ExpressionModel</code> is the {@link ProgramElementModel} for an expression.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class ExpressionModel extends BaseProgramElementModel<ExpressionModel>
   {
   private static final Logger LOG = Logger.getLogger(ExpressionModel.class);

   public static final String DELAY_IN_MILLIS_PROPERTY = "delayInMillis";
   public static final float MIN_DELAY_VALUE_IN_SECS = 0;
   public static final float MAX_DELAY_VALUE_IN_SECS = 999.99f;
   public static final int MIN_DELAY_VALUE_IN_MILLIS = (int)(MIN_DELAY_VALUE_IN_SECS * 1000);
   public static final int MAX_DELAY_VALUE_IN_MILLIS = (int)(MAX_DELAY_VALUE_IN_SECS * 1000);

   private final File expressionFile;
   private final XmlExpression xmlExpression;
   private int delayInMillis = 0;

   /**
    * Creates an <code>ExpressionModel</code> for the given <code>expressionFile</code> with an empty comment and no
    * delay.
    */
   public ExpressionModel(@NotNull final File expressionFile)
      {
      this(expressionFile, null, 0);
      }

   /**
    * Creates an <code>ExpressionModel</code> for the given <code>expressionFile</code> with the given
    * <code>comment</code> given <code>delayInMillis</code>. This constructor ensures that the delay is within the range
    * <code>[{@link #MIN_DELAY_VALUE_IN_MILLIS}, {@link #MAX_DELAY_VALUE_IN_MILLIS}]</code>.
    */
   public ExpressionModel(@NotNull final File expressionFile, @Nullable final String comment, final int delayInMillis)
      {
      super(comment);
      this.expressionFile = expressionFile;
      this.delayInMillis = cleanDelayInMillis(delayInMillis);
      try
         {
         this.xmlExpression = XmlExpression.create(expressionFile);
         }
      catch (IOException e)
         {
         LOG.error("IOException while trying to create the XmlExpression, rethrowing as an IllegalArgumentException", e);
         throw new IllegalArgumentException("IOException while trying to create the XmlExpression", e);
         }
      catch (JDOMException e)
         {
         LOG.error("JDOMException while trying to create the XmlExpression, rethrowing as an IllegalArgumentException", e);
         throw new IllegalArgumentException("JDOMException while trying to create the XmlExpression", e);
         }
      }

   /** Copy constructor */
   private ExpressionModel(@NotNull final ExpressionModel originalExpressionModel)
      {
      this(originalExpressionModel.getExpressionFile(),
           originalExpressionModel.getComment(),
           originalExpressionModel.getDelayInMillis());
      }

   /** Returns the expression's file name, without the .xml extension. */
   @Override
   @NotNull
   public String getName()
      {
      // get the filename, but strip off any .xml extension
      String fileName = expressionFile.getName();
      if (fileName.toLowerCase().lastIndexOf(".xml") != -1)
         {
         fileName = fileName.substring(0, fileName.lastIndexOf('.'));
         }

      return fileName;
      }

   @Override
   public boolean isContainer()
      {
      return false;
      }

   @Override
   @NotNull
   public ExpressionModel createCopy()
      {
      return new ExpressionModel(this);
      }

   public File getExpressionFile()
      {
      return expressionFile;
      }

   public XmlExpression getXmlExpression()
      {
      return xmlExpression;
      }

   public int getDelayInMillis()
      {
      return delayInMillis;
      }

   /**
    * Sets the delay in milliseconds, and causes a {@link PropertyChangeEvent} to be fired for the
    * {@link #DELAY_IN_MILLIS_PROPERTY} property.  This method ensures that the value is within the range
    * <code>[{@link #MIN_DELAY_VALUE_IN_MILLIS}, {@link #MAX_DELAY_VALUE_IN_MILLIS}]</code>.
    */
   public void setDelayInMillis(final int delayInMillis)
      {
      final int cleanedDelayInMillis = cleanDelayInMillis(delayInMillis);
      final PropertyChangeEvent event = new PropertyChangeEventImpl(DELAY_IN_MILLIS_PROPERTY, this.delayInMillis, cleanedDelayInMillis);
      this.delayInMillis = cleanedDelayInMillis;
      firePropertyChangeEvent(event);
      }

   private int cleanDelayInMillis(final int delayInMillis)
      {
      int cleanedDelayInMillis = delayInMillis;
      if (delayInMillis < MIN_DELAY_VALUE_IN_MILLIS)
         {
         cleanedDelayInMillis = MIN_DELAY_VALUE_IN_MILLIS;
         }
      else if (delayInMillis > MAX_DELAY_VALUE_IN_MILLIS)
         {
         cleanedDelayInMillis = MAX_DELAY_VALUE_IN_MILLIS;
         }
      return cleanedDelayInMillis;
      }

   @Override
   public boolean equals(final Object o)
      {
      if (this == o)
         {
         return true;
         }
      if (o == null || getClass() != o.getClass())
         {
         return false;
         }
      if (!super.equals(o))
         {
         return false;
         }

      final ExpressionModel that = (ExpressionModel)o;

      if (delayInMillis != that.delayInMillis)
         {
         return false;
         }
      if (expressionFile != null ? !expressionFile.equals(that.expressionFile) : that.expressionFile != null)
         {
         return false;
         }
      if (xmlExpression != null ? !xmlExpression.equals(that.xmlExpression) : that.xmlExpression != null)
         {
         return false;
         }

      return true;
      }

   @Override
   public int hashCode()
      {
      int result = super.hashCode();
      result = 31 * result + (expressionFile != null ? expressionFile.hashCode() : 0);
      result = 31 * result + (xmlExpression != null ? xmlExpression.hashCode() : 0);
      result = 31 * result + delayInMillis;
      return result;
      }
   }
