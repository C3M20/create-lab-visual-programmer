package edu.cmu.ri.createlab.terk.expression.manager;

import java.io.File;
import java.io.IOException;
import edu.cmu.ri.createlab.terk.expression.XmlExpression;
import edu.cmu.ri.createlab.visualprogrammer.PathManager;
import org.apache.log4j.Logger;
import org.jdom.JDOMException;
import org.jetbrains.annotations.Nullable;

/**
 * Simple wrapper for an {@link XmlExpression} and the {@link File} with which it's associated.
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class ExpressionFile implements Comparable<ExpressionFile>
   {
   private static final Logger LOG = Logger.getLogger(ExpressionFile.class);

   private final String filename;

   public ExpressionFile(final String file)
      {
      this.filename = file;
      }

   @Nullable
   public XmlExpression getExpression()
      {
      try
         {
         return XmlExpression.create(PathManager.getInstance().getExpressionsZipSave().getFile_InputStream(filename));
         }
      catch (IOException e)
         {
         LOG.error("IOException while trying to load the expression for file [" + filename + "]", e);
         }
      catch (JDOMException e)
         {
         LOG.error("JDOMException while trying to load the expression for file [" + filename + "]", e);
         }
      return null;
      }

   public String getFileName()
      {
      return filename;
      }

   /** Returns the expression's file name, without the .xml extension. */
   public String getPrettyName()
      {
      // get the filename, but strip off any .xml extension
      String fileName = this.filename;
      if (fileName.toLowerCase().lastIndexOf(".xml") != -1)
         {
         fileName = fileName.substring(0, fileName.lastIndexOf('.'));
         }

      return fileName;
      }

   /**
    * Determines whether this instance is equal to the given instance.  Comparison is only performed on the {@link File}.
    *
    * @see #compareTo
    */
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

      final ExpressionFile that = (ExpressionFile)o;

      return !(filename != null ? !filename.equals(that.filename) : that.filename != null);
      }

   @Override
   public int hashCode()
      {
      return filename != null ? filename.hashCode() : 0;
      }

   /**
    * Comparison is performed on the {@link File} only, thus this class has a natural ordering that is consistent with
    * {@link #equals equals()} since {@link #equals equals()} also only uses the {@link File}.
    *
    * @see #equals(Object)
    */
   public int compareTo(final ExpressionFile that)
      {
      // yes, I really meant to use == and not .equals() here, since I want to check equivalence first
      if (this == that)
         {
         return 0;
         }

      return this.filename.compareTo(that.getFileName());
      }
   }
