package org.rainbowlabs.ev3dev;

import com.jcraft.jsch.*;
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

    @Parameter(property = "javaDir", defaultValue = "/home/robot/java")
    private String javaDestination;

    public void execute() throws MojoExecutionException, MojoFailureException {

        try {
            ChannelSftp sftp = setupSshConnection();
            uploadJar(sftp);
        } catch (JSchException e) {
            getLog().error("SSH session with brick failed. Maybe check your input.", e);
        } catch (SftpException e) {
            getLog().error("Uploading files failed", e);
        }
        getLog().info("Destination: " + javaDestination);
        getLog().info("CLASS: " + mainClass + " HOST: " + ipAddress + " USER: " + user + " PASS: " + password);
    }

    private ChannelSftp setupSshConnection() throws JSchException {
        JSch jSch = new JSch();
        Session session = jSch.getSession(user, ipAddress);
        session.setPassword(password);
        session.connect();

        return (ChannelSftp) session.openChannel("sftp");
    }

    private void uploadJar(ChannelSftp sftp) throws SftpException {
        sftp.put("target/*.jar", javaDestination);
    }
}
