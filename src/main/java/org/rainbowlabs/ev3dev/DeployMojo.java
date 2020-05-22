package org.rainbowlabs.ev3dev;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "deploy", defaultPhase = LifecyclePhase.INSTALL)
public class DeployMojo extends AbstractMojo {

    @Parameter(property = "mainClass")
    private String mainClass;

    @Parameter(property = "sshHost")
    private String ipAddress;

    @Parameter(property = "sshUser")
    private String user;

    @Parameter(property = "sshPassword")
    private String password;

    @Parameter(property = "wrapperDir", defaultValue = "/home/robot")
    private String wrapperDirectory;

    @Parameter(property = "javaDir", defaultValue = "/home/robot/java")
    private String javaDirectory;

    @Parameter(property = "libraryDir", defaultValue = "/home/robot/java/libraries")
    private String libraryDirectory;

    @Parameter(property = "programDir", defaultValue = "/home/robot/java/programs")
    private String programDirectory;

    @Parameter(property = "splashDir", defaultValue = "/home/robot/java/splashes")
    private String splashDirectory;

    @Parameter(property = "opencvJar", defaultValue = "/usr/share/java/opencv.jar")
    private String opencvJar;

    @Parameter(property = "rxtxJar", defaultValue = "usr/share/java/RXTXcomm.jar")
    private String rxtxJar;

    public void execute() throws MojoExecutionException, MojoFailureException {

        ChannelSftp sftp = null;
        try {
            sftp = setupSshConnection();

            if (createDirectories(sftp)) {
                deployDependencies(sftp);
                deployJar(sftp);
                deplyLauncher(sftp);
            }
        } catch (JSchException e) {
            getLog().error("SSH session with brick failed. Maybe check your input.", e);
        } catch (SftpException e) {
            getLog().error("Uploading files failed", e);
        } finally {
            getLog().info("Closing sftp connection to the brick");
            if (sftp != null && sftp.isConnected()) {
                sftp.disconnect();
            }
        }
        getLog().info("Destination: " + javaDirectory);
        getLog().info("CLASS: " + mainClass + " HOST: " + ipAddress + " USER: " + user + " PASS: " + password);
    }

    private ChannelSftp setupSshConnection() throws JSchException {
        JSch jSch = new JSch();
        Session session = jSch.getSession(user, ipAddress);
        session.setPassword(password);
        session.connect();

        return (ChannelSftp) session.openChannel("sftp");
    }

    private boolean createDirectories(ChannelSftp sftp) throws SftpException {
        getLog().info("Creating directory structure on the EV3");
        sftp.mkdir(wrapperDirectory);
        sftp.mkdir(javaDirectory);
        sftp.mkdir(splashDirectory);
        sftp.mkdir(programDirectory);
        sftp.mkdir(libraryDirectory);


        return true;
    }

    private void deployDependencies(ChannelSftp sftp) throws SftpException {
        getLog().info("Uploading dependencies to the EV3");
        sftp.put("target", libraryDirectory);
    }

    private void deployJar(ChannelSftp sftp) throws SftpException {
        getLog().info("Uploading application jar file to the EV3");
        sftp.put("target/*.jar", javaDirectory);
    }

    private void deplyLauncher(ChannelSftp sftp) throws SftpException {
        getLog().info("Uploading launcher file to the EV3");
        sftp.put("", wrapperDirectory);
    }
}
