package com.gewara.helper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.util.Store;

public final class PassSignUtil {
	private PassSignUtil() {
	}
	static{
		if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
			Security.addProvider(new BouncyCastleProvider());
		}
	}
	private static PassSignInfo signInfo;
	public static byte[] sign(byte[] content) throws Exception{
		if(signInfo == null){
			String pk12 =  PassSignUtil.class.getClassLoader().getResource("com/gewara/passbook/ca/GewaraMoviePass.p12").getFile();
			String chain =  PassSignUtil.class.getClassLoader().getResource("com/gewara/passbook/ca/AppleWWDRCA.cer").getFile();
			signInfo = loadSignInfFromPKCS12FileAndIntermediateCAFile(pk12, "gewara", chain);
		}
		byte[] result = sign(content, signInfo);
		return result;
	}
	private static byte[] sign(byte[] content, final PassSignInfo signingInformation) throws Exception {
		CMSSignedDataGenerator generator = new CMSSignedDataGenerator();
		ContentSigner sha1Signer = new JcaContentSignerBuilder("SHA1withRSA").setProvider(BouncyCastleProvider.PROVIDER_NAME).build(
				signingInformation.getSigningPrivateKey());

		generator.addSignerInfoGenerator(new JcaSignerInfoGeneratorBuilder(new JcaDigestCalculatorProviderBuilder().setProvider(
				BouncyCastleProvider.PROVIDER_NAME).build()).build(sha1Signer, signingInformation.getSigningCert()));

		List<X509Certificate> certList = new ArrayList<X509Certificate>();
		certList.add(signingInformation.getAppleWWDRCACert());
		certList.add(signingInformation.getSigningCert());

		Store certs = new JcaCertStore(certList);

		generator.addCertificates(certs);

		CMSSignedData sigData = generator.generate(new CMSProcessableByteArray(content), false);
		byte[] signedDataBytes = sigData.getEncoded();

		ByteArrayOutputStream signed = new ByteArrayOutputStream();
		signed.write(signedDataBytes);
		signed.close();
		return signed.toByteArray();
	}

	private static PassSignInfo loadSignInfFromPKCS12FileAndIntermediateCAFile(final String pkcs12KeyStoreFilePath,
			final String keyStorePassword, final String appleWWDRCAFilePath) throws IOException, NoSuchAlgorithmException, CertificateException,
			KeyStoreException, UnrecoverableKeyException {
		KeyStore pkcs12KeyStore = loadPKCS12File(pkcs12KeyStoreFilePath, keyStorePassword);
		Enumeration<String> aliases = pkcs12KeyStore.aliases();

		PrivateKey signingPrivateKey = null;
		X509Certificate signingCert = null;

		while (aliases.hasMoreElements()) {
			String aliasName = aliases.nextElement();

			Key key = pkcs12KeyStore.getKey(aliasName, keyStorePassword.toCharArray());
			if (key instanceof PrivateKey) {
				signingPrivateKey = (PrivateKey) key;
				Object cert = pkcs12KeyStore.getCertificate(aliasName);
				if (cert instanceof X509Certificate) {
					signingCert = (X509Certificate) cert;
					break;
				}
			}
		}

		X509Certificate appleWWDRCACert = loadDERCertificate(appleWWDRCAFilePath);
		if (signingCert == null || signingPrivateKey == null || appleWWDRCACert == null) {
			throw new IOException("Couldn#t load all the neccessary certificates/keys");
		}

		return new PassSignInfo(signingCert, signingPrivateKey, appleWWDRCACert);
	}

	private static KeyStore loadPKCS12File(final String pathToP12, final String password) throws IOException, NoSuchAlgorithmException,
			CertificateException, KeyStoreException {
		KeyStore keystore = KeyStore.getInstance("PKCS12");
		File p12File = new File(pathToP12);
		if (!p12File.exists()) {
			// try loading it from the classpath
			URL localP12File = PassSignUtil.class.getClassLoader().getResource(pathToP12);
			if (localP12File == null) {
				throw new FileNotFoundException("File at " + pathToP12 + " not found");
			}
			p12File = new File(localP12File.getFile());
		}
		InputStream streamOfFile = new FileInputStream(p12File);

		keystore.load(streamOfFile, password.toCharArray());
		return keystore;
	}

	private static X509Certificate loadDERCertificate(final String filePath) throws IOException, CertificateException {
		FileInputStream certificateFileInputStream = null;
		try {
			File certFile = new File(filePath);
			if (!certFile.exists()) {
				// try loading it from the classpath
				URL localCertFile = PassSignUtil.class.getClassLoader().getResource(filePath);
				if (localCertFile == null) {
					throw new FileNotFoundException("File at " + filePath + " not found");
				}
				certFile = new File(localCertFile.getFile());
			}
			certificateFileInputStream = new FileInputStream(certFile);

			CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509", BouncyCastleProvider.PROVIDER_NAME);
			Certificate certificate = certificateFactory.generateCertificate(certificateFileInputStream);
			if (certificate instanceof X509Certificate) {
				return (X509Certificate) certificate;
			}
			throw new IOException("The key from '" + filePath + "' could not be decrypted");
		} catch (IOException ex) {
			throw new IOException("The key from '" + filePath + "' could not be decrypted", ex);
		} catch (NoSuchProviderException ex) {
			throw new IOException("The key from '" + filePath + "' could not be decrypted", ex);
		} finally {
			IOUtils.closeQuietly(certificateFileInputStream);
		}
	}

	private static class PassSignInfo {
	    private X509Certificate signingCert;
	    private PrivateKey signingPrivateKey;
	    private X509Certificate appleWWDRCACert;
	    public PassSignInfo(final X509Certificate signingCert, final PrivateKey signingPrivateKey, final X509Certificate appleWWDRCACert) {
	        this.signingCert = signingCert;
	        this.signingPrivateKey = signingPrivateKey;
	        this.appleWWDRCACert = appleWWDRCACert;
	    }

	    public X509Certificate getSigningCert() {
	        return signingCert;
	    }

	    public PrivateKey getSigningPrivateKey() {
	        return signingPrivateKey;
	    }
	    public X509Certificate getAppleWWDRCACert() {
	        return appleWWDRCACert;
	    }
	}

	public static void main(String[] args) {
		try {
			PassSignInfo info = loadSignInfFromPKCS12FileAndIntermediateCAFile("F:/test/new.p12", "gewara",
					"F:/test/AppleWWDRCA.cer");
			byte[] result = sign("sdlfjwelkjsfldkjfwe".getBytes(), info);
			System.out.println(Hex.encodeHexString(result));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
