Creating a Keystore and Self-Signed Certificate
------------------------------------------------
Here's what I did to create the keystore so we can sign the jars with a self-signed certificate.

First, create the keystore by doing the following in the Terminal, with the current directory set to this directory.
When prompted, I used the password "password".

   $ keytool -genkey -keystore CREATELabKeystore -alias createlab
     Enter keystore password:
     Re-enter new password:
     What is your first and last name?
       [Unknown]:  CREATE Lab
     What is the name of your organizational unit?
       [Unknown]:  Robotics Institute
     What is the name of your organization?
       [Unknown]:  Carnegie Mellon University
     What is the name of your City or Locality?
       [Unknown]:  Pittsburgh
     What is the name of your State or Province?
       [Unknown]:  Pennsylvania
     What is the two-letter country code for this unit?
       [Unknown]:  US
     Is CN=CREATE Lab, OU=Robotics Institute, O=Carnegie Mellon University, L=Pittsburgh, ST=Pennsylvania, C=US correct?
       [no]:  yes

     Enter key password for <createlab>
      (RETURN if same as keystore password):

Next, create a self-signed certificate.  The "validity" flag specifies for how many days the certificate is valid.  I
chose 342 here so that it would expire on 2012-12-12.

   $ keytool -selfcert -validity 342 -alias createlab -keystore CREATELabKeystore
     Enter keystore password:

This last step is optional, and is just used to verify that everything worked OK.

   $ keytool -list -keystore CREATELabKeystore
     Enter keystore password:

     Keystore type: JKS
     Keystore provider: SUN

     Your keystore contains 1 entry

     createlab, Jan 5, 2012, PrivateKeyEntry,
     Certificate fingerprint (MD5): 5C:B1:53:12:C6:84:1C:E3:E1:EF:61:37:F1:EF:4E:DA

------------------------------------------------------------------------------------------------------------------------
Re-signing the certificate so that it expires on 2013-12-11:

   $ keytool -selfcert -validity 376 -alias createlab -keystore CREATELabKeystore
     Enter keystore password:

Just to verify...

   $ keytool -list -keystore CREATELabKeystore
   Enter keystore password:

   Keystore type: JKS
   Keystore provider: SUN

   Your keystore contains 1 entry

   createlab, Nov 30, 2012, PrivateKeyEntry,
   Certificate fingerprint (MD5): 57:14:39:AA:54:DF:7E:4D:3E:49:58:90:CF:1C:D5:7C

------------------------------------------------------------------------------------------------------------------------
Re-signing the certificate so that it expires on 2014-12-13:

   $ keytool -selfcert -validity 368 -alias createlab -keystore CREATELabKeystore
     Enter keystore password:

Just to verify...

   $ keytool -list -keystore CREATELabKeystore
     Enter keystore password:

     Keystore type: JKS
     Keystore provider: SUN

     Your keystore contains 1 entry

     createlab, Dec 10, 2013, PrivateKeyEntry,
     Certificate fingerprint (MD5): 19:BB:11:C2:B8:F0:1E:1F:FA:79:F3:2C:67:7A:DA:A5

