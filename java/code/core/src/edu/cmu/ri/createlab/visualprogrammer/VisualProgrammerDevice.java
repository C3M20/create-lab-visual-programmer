package edu.cmu.ri.createlab.visualprogrammer;

import java.util.Collection;
import java.util.Set;
import javax.swing.ImageIcon;
import edu.cmu.ri.createlab.device.CreateLabDeviceProxy;
import edu.cmu.ri.createlab.expressionbuilder.ExpressionBuilderDevice;
import edu.cmu.ri.createlab.sequencebuilder.SequenceBuilderDevice;
import edu.cmu.ri.createlab.terk.services.ServiceManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * <p>
 * <code>VisualProgrammerDevice</code> defines methods required by any device that is to be controlled by the
 * Visual Programmer.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface VisualProgrammerDevice
   {
   interface SensorListener
      {
      void processSensorRawValue(@NotNull final String sensorServiceTypeId, final int portNumber, @NotNull final Object rawValue);
      }

   /** Returns a "pretty name" for the device, for example one which could be shown to an end user. */
   String getDeviceName();

   /** Returns a description of the device version.  Format is up to the implementation.  May be <code>null</code>. */
   @Nullable
   String getDeviceVersion();

   // TODO: return a JPanel with the connection animations in it instead--this is a lame hack because I'm pressed for time
   ImageIcon getConnectingImage();

   ImageIcon getConnectionTipsImage();

   /** Connects to the device.  This method must block until the connection is complete. */
   void connect();

   /** Returns whether the device is currently connected */
   boolean isConnected();

   /** Returns the device's {@link CreateLabDeviceProxy}, or <code>null</code> if the device isn't connected. */
   @Nullable
   CreateLabDeviceProxy getDeviceProxy();

   /** Returns the device's {@link ServiceManager}, or <code>null</code> if the device isn't connected. */
   @Nullable
   ServiceManager getServiceManager();

   @NotNull
   ExpressionBuilderDevice getExpressionBuilderDevice();

   @NotNull
   SequenceBuilderDevice getSequenceBuilderDevice();

   /**
    * Returns an unmodifiable {@link Collection} of {@link Sensor}s supported by this device.  Guaranteed to not
    * return <code>null</code>, but may return an empty collection.  Will return an empty collection if there are no
    * sensor types, or if this method is called before a connection has been established.
    */
   @NotNull
   Collection<Sensor> getSensors();

   /**
    * Returns the sensor matching the given <code>sensorName</code> and <code>serviceTypeId</code>.  Returns
    * <code>null</code> if no match is found.
    */
   @Nullable
   Sensor findSensor(@Nullable final String sensorName, @Nullable final String serviceTypeId);

   /**
    * Registers the given {@link SensorListener}.  Does nothing if the {@link SensorListener} is <code>null</code>.
    */
   void addSensorListener(@Nullable final SensorListener listener);

   /**
    * Unregisters the given {@link SensorListener}.  Does nothing if the {@link SensorListener} is <code>null</code>.
    */
   void removeSensorListener(@Nullable final SensorListener listener);

   /**
    * Returns a (possibly empty or <code>null</code>) unmodifiable {@link Set} of language names to which we support
    * exporting expressions/sequences for this device.
    */
   @Nullable
   Set<String> getExportableLanguages();

   /** Disconnects from the device. */
   void disconnect();
   }