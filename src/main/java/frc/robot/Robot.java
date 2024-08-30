// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;



import java.util.Optional;

// Imports that allow the usage of REV Spark Max motor controllers
import com.revrobotics.CANSparkBase;
import com.revrobotics.CANSparkBase.IdleMode;
import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkLowLevel.MotorType;

import edu.wpi.first.wpilibj.AddressableLED;
import edu.wpi.first.wpilibj.AddressableLEDBuffer;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import edu.wpi.first.wpilibj.DutyCycleEncoder;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;



public class Robot extends TimedRobot {
  /*
   * Autonomous selection options.
   */
  private static final String kNothingAuto = "do nothing";
  private static final String kLaunchAndDriveAmp = "launch drive";
  private static final String kLaunchAndDrive = "launch drive wide";
  private static final String kLaunch = "launch";
  private static final String kDrive = "drive";
  private static final String kdoubleCenter = "2 note center";
  private String m_autoSelected;
  private final SendableChooser<String> m_chooser = new SendableChooser<>();





  /*
   * Drive motor controller instances.
   *
   * Change the id's to match your robot.
   * Change kBrushed to kBrushless if you are uisng NEOs.
   * The rookie kit comes with CIMs which are brushed motors.
   * Use the appropriate other class if you are using different controllers.
   */
  CANSparkBase leftRear = new CANSparkMax(2, MotorType.kBrushed);
  CANSparkBase leftFront = new CANSparkMax(1, MotorType.kBrushed);
  CANSparkBase rightRear = new CANSparkMax(3, MotorType.kBrushed);
  CANSparkBase rightFront = new CANSparkMax(4, MotorType.kBrushed);

  /*
   * A class provided to control your drivetrain. Different drive styles can be passed to differential drive:
   * https://github.com/wpilibsuite/allwpilib/blob/main/wpilibj/src/main/java/edu/wpi/first/wpilibj/drive/DifferentialDrive.java
   */
  DifferentialDrive m_drivetrain;

  /*
   * Launcher motor controller instances.
   *
   * Like the drive motors, set the CAN id's to match your robot or use different
   * motor controller classses (VictorSPX) to match your robot as necessary.
   *
   * Both of the motors used on the KitBot launcher are CIMs which are brushed motors
   */
  CANSparkBase m_launchWheel = new CANSparkMax(5, MotorType.kBrushless);
  CANSparkBase m_feedWheel = new CANSparkMax(6, MotorType.kBrushless);
  /**
   * Roller Claw motor controller instance.
  */
  CANSparkBase m_rollerClaw = new CANSparkMax(8, MotorType.kBrushless);
  CANSparkMax m_pivotMotor = new CANSparkMax(7,MotorType.kBrushless);
  DutyCycleEncoder encoder = new DutyCycleEncoder(0);
  CANSparkBase m_climber_1 = new CANSparkMax(9, MotorType.kBrushed);
  CANSparkBase m_climber_2 = new CANSparkMax(10, MotorType.kBrushed);
  
  
  /**
   * Climber motor controller instance. In the stock Everybot configuration a
   * NEO is used, replace with kBrushed if using a brushed motor.
   */
  /*CANSparkBase m_climber = new CANSparkMax(7, MotorType.kBrushless);*/
  

    /**
   * The starter code uses the most generic joystick class.
   *
   * To determine which button on your controller corresponds to which number, open the FRC
   * driver station, go to the USB tab, plug in a controller and see which button lights up
   * when pressed down
   *
   * Buttons index from 0
   */
  Joystick m_driverController = new Joystick(0);


  Joystick m_manipController = new Joystick(1);


  // --------------- Magic numbers. Use these to adjust settings. ---------------

 /**
   * How many amps can an individual drivetrain motor use.
   */
  static final int DRIVE_CURRENT_LIMIT_A = 60;

  /**
   * How many amps the feeder motor can use.
   */
  static final int FEEDER_CURRENT_LIMIT_A = 80;

  /**
   * Percent output to run the feeder when expelling note
   */
  static final double FEEDER_OUT_SPEED = 1.5;

  /**
   * Percent output to run the feeder when intaking note
   */
  static final double FEEDER_IN_SPEED = -.4;

  /**
   * Percent output for amp or drop note, configure based on polycarb bend
   */
  static final double FEEDER_AMP_SPEED = .4;

  /**
   * How many amps the launcher motor can use.
   *
   * In our testing we favored the CIM over NEO, if using a NEO lower this to 60
   */
  static final int LAUNCHER_CURRENT_LIMIT_A = 80;

  /**
   * Percent output to run the launcher when intaking AND expelling note
   */
  static final double LAUNCHER_SPEED = 1.5;

  /**
   * Percent output for scoring in amp or dropping note, configure based on polycarb bend
   * .14 works well with no bend from our testing
   */
  static final double LAUNCHER_AMP_SPEED = .17;
  /**
   * Percent output for the roller claw
   */
  static final double CLAW_OUTPUT_POWER = 0.2;
  /**
   * Percent output to help retain notes in the claw
   */
  static final double CLAW_STALL_POWER = 0;
  /**
   * Percent output to power the climber
   */
  static final double CLIMER_OUTPUT_POWER = 1;

  static final float PIVOT_MIN = 62;
  static final float PIVOT_MAX = 278;
  AddressableLED m_led;
  AddressableLEDBuffer m_ledBuffer;
  int m_rainbowFirstPixelHue;

  /**
   * This function is run when the robot is first started up and should be used for any
   * initialization code.
   */
  @Override
  public void robotInit() {
    
    
    m_chooser.setDefaultOption("do nothing", kNothingAuto);
    m_chooser.addOption("launch note and drive Amp", kLaunchAndDriveAmp);
        m_chooser.addOption("launch note and drive", kLaunchAndDrive);
    m_chooser.addOption("2 note center", kdoubleCenter);
    m_chooser.addOption("launch", kLaunch);
    m_chooser.addOption("drive", kDrive);
    SmartDashboard.putData("Auto choices", m_chooser);
    
    



    /*
     * Apply the current limit to the drivetrain motors
     */
    leftRear.setSmartCurrentLimit(DRIVE_CURRENT_LIMIT_A);
    leftFront.setSmartCurrentLimit(DRIVE_CURRENT_LIMIT_A);
    rightRear.setSmartCurrentLimit(DRIVE_CURRENT_LIMIT_A);
    rightFront.setSmartCurrentLimit(DRIVE_CURRENT_LIMIT_A);

    /*
     * Tells the rear wheels to follow the same commands as the front wheels
     */
    leftRear.follow(leftFront);
    rightRear.follow(rightFront);

    /*
     * One side of the drivetrain must be inverted, as the motors are facing opposite directions
     */
    leftFront.setInverted(true);
    rightFront.setInverted(false);

    m_drivetrain = new DifferentialDrive(leftFront, rightFront);

    /*
     * Launcher wheel(s) spinning the wrong direction? Change to true here.
     *
     * Add white tape to wheel to help determine spin direction.
     */
    //m_feedWheel.setInverted(true);
    m_launchWheel.setInverted(true);
    /*
     * Apply the current limit to the launching mechanism
     */
    m_feedWheel.setSmartCurrentLimit(FEEDER_CURRENT_LIMIT_A);
    m_launchWheel.setSmartCurrentLimit(LAUNCHER_CURRENT_LIMIT_A);
    /*
     * Inverting and current limiting for roller claw and climber
     */
    m_rollerClaw.setInverted(false);
    m_climber_1.setInverted(false);
    m_climber_2.setInverted(false);

    m_rollerClaw.setSmartCurrentLimit(30);

    m_climber_1.setSmartCurrentLimit(60);
    m_climber_2.setSmartCurrentLimit(60);
    /*
     * Motors can be set to idle in brake or coast mode.
     * 
     * Brake mode is best for these mechanisms
     */
    m_rollerClaw.setIdleMode(IdleMode.kBrake);
    m_climber_1.setIdleMode(IdleMode.kBrake);
    m_climber_2.setIdleMode(IdleMode.kBrake);
    m_feedWheel.setIdleMode(IdleMode.kCoast);
    m_launchWheel.setIdleMode(IdleMode.kCoast);
    m_pivotMotor.setIdleMode(IdleMode.kBrake);
    m_led = new AddressableLED(9);
    m_ledBuffer = new AddressableLEDBuffer(70);
    m_led.setLength(m_ledBuffer.getLength());
    m_led.setData(m_ledBuffer);
    m_led.start();
    setSoftLimits();
  }

  /**
   * This function is called every 20 ms, no matter the mode. Use this for items like diagnostics
   * that you want ran during disabled, autonomous, teleoperated and test modes.
   */
  @Override
  public void robotPeriodic() {
    SmartDashboard.putNumber("Time (seconds)", Timer.getFPGATimestamp());
    SmartDashboard.putNumber("Pivot Encoder", encoder.getAbsolutePosition() * 360);

  }


  /*
   * Auto constants, change values below in autonomousInit()for different autonomous behaviour
   *
   * A delayed action starts X seconds into the autonomous period
   *
   * A time action will perform an action for X amount of seconds
   *
   * Speeds can be changed as desired and will be set to 0 when
   * performing an auto that does not require the system
   */
  double AUTO_LAUNCH_DELAY_S;
  double AUTO_DRIVE_DELAY_S;

  double AUTO_DRIVE_TIME_S;

  double AUTO_DRIVE_SPEED;
  double AUTO_LAUNCHER_SPEED;

  double autonomousStartTime;
  
  // LED's
  @Override
  public void disabledPeriodic() {
    double breatheSpeed = 175;
      int r = (int) (Math.pow(Math.sin(System.currentTimeMillis() / 1000.0), 2) * breatheSpeed);
      Optional<Alliance> ally = DriverStation.getAlliance();
    if (ally.isPresent()) {
      if (ally.get() == Alliance.Red) {
        for (var i = 0; i < m_ledBuffer.getLength(); i++) {
        // Sets the specified LED to the HSV values for red
        m_ledBuffer.setRGB(i, r, 0, 0);
      }
      m_led.setData(m_ledBuffer);
      }
      if (ally.get() == Alliance.Blue) {
        for (var i = 0; i < m_ledBuffer.getLength(); i++) {
        // Sets the specified LED to the HSV values for red
        m_ledBuffer.setRGB(i, 0, 0, r);
      }
      m_led.setData(m_ledBuffer);
      }
    }
    else {
      for (var i = 0; i < m_ledBuffer.getLength(); i++) {
        // Sets the specified LED to the HSV values for red
        m_ledBuffer.setRGB(i, r, 0, r);
      }
      m_led.setData(m_ledBuffer);
      
    }
      
  }
  

  
  @Override
  public void autonomousInit() {
    
    m_autoSelected = m_chooser.getSelected();

    leftRear.setIdleMode(IdleMode.kBrake);
    leftFront.setIdleMode(IdleMode.kBrake);
    rightRear.setIdleMode(IdleMode.kBrake);
    rightFront.setIdleMode(IdleMode.kBrake);

    AUTO_LAUNCH_DELAY_S = 1.5;
    AUTO_DRIVE_DELAY_S = 1;

    AUTO_DRIVE_TIME_S = 1.8;
    AUTO_DRIVE_SPEED = -0.5;
    AUTO_LAUNCHER_SPEED = 1;
    
    /*
     * Depeding on which auton is selected, speeds for the unwanted subsystems are set to 0
     * if they are not used for the selected auton
     *
     * For kDrive you can also change the kAutoDriveBackDelay
     */
    if (m_autoSelected == kdoubleCenter){
      double breatheSpeed = 400;
      int r = (int) (Math.pow(Math.sin(System.currentTimeMillis() / 1000.0), 2) * breatheSpeed);
      for (var i = 0; i < m_ledBuffer.getLength(); i++) {
        // Sets the specified LED to the HSV values for red
        m_ledBuffer.setRGB(i, r, 0, r);
      }
      m_led.setData(m_ledBuffer);
    }
    if(m_autoSelected == kLaunchAndDrive){
      for (var i = 0; i < m_ledBuffer.getLength(); i++) {
        // Sets the specified LED to the HSV values for red
        m_ledBuffer.setRGB(i, 0, 255, 35);
     }
     m_led.setData(m_ledBuffer);
    }
    if(m_autoSelected == kLaunchAndDriveAmp){
      for (var i = 0; i < m_ledBuffer.getLength(); i++) {
        // Sets the specified LED to the HSV values for red
        m_ledBuffer.setRGB(i, 0, 0, 255);
     }
     m_led.setData(m_ledBuffer);
    }
    if(m_autoSelected == kLaunch)
    {
      AUTO_DRIVE_SPEED = 0;
      m_drivetrain.arcadeDrive(0, 0);
      for (var i = 0; i < m_ledBuffer.getLength(); i++) {
        // Sets the specified LED to the HSV values for red
        m_ledBuffer.setRGB(i, 0, 255, 0);
     }
     m_led.setData(m_ledBuffer);
    }
    else if(m_autoSelected == kDrive)
    {
      m_drivetrain.arcadeDrive(0, 0);
      for (var i = 0; i < m_ledBuffer.getLength(); i++) {
        // Sets the specified LED to the HSV values for red
        m_ledBuffer.setRGB(i, 255, 0, 0);
     }
     m_led.setData(m_ledBuffer);
      AUTO_LAUNCHER_SPEED = 0;
    }
    else if(m_autoSelected == kNothingAuto)
    {
      m_drivetrain.arcadeDrive(0, 0);
      for (var i = 0; i < m_ledBuffer.getLength(); i++) {
        // Sets the specified LED to the HSV values for red
        m_ledBuffer.setRGB(i, 0, 0, 255);
     }
     m_led.setData(m_ledBuffer);
      AUTO_DRIVE_SPEED = 0;
      AUTO_LAUNCHER_SPEED = 0;
    }

    autonomousStartTime = Timer.getFPGATimestamp();
    
  }

  /** This function is called periodically during autonomous. */
  @Override
  public void autonomousPeriodic() {
    Optional<Alliance> ally = DriverStation.getAlliance();

    double timeElapsed = Timer.getFPGATimestamp() - autonomousStartTime;

    /*
     * Spins up launcher wheel until time spent in auto is greater than AUTO_LAUNCH_DELAY_S
     *
     * Feeds note to launcher until time is greater than AUTO_DRIVE_DELAY_S
     *
     * Drives until time is greater than AUTO_DRIVE_DELAY_S + AUTO_DRIVE_TIME_S
     *
     * Does not move when time is greater than AUTO_DRIVE_DELAY_S + AUTO_DRIVE_TIME_S
     */

    if (m_autoSelected == kLaunchAndDriveAmp && ally.get()== Alliance.Red){
    if(timeElapsed < AUTO_LAUNCH_DELAY_S)
    {
      m_launchWheel.set(LAUNCHER_SPEED);
      m_feedWheel.set(LAUNCHER_SPEED);
      m_rollerClaw.set(0);
      m_drivetrain.arcadeDrive(0, 0);

    }
    else if(timeElapsed < AUTO_DRIVE_DELAY_S + AUTO_DRIVE_TIME_S)
    {
      m_launchWheel.set(LAUNCHER_SPEED);
      m_feedWheel.set(LAUNCHER_SPEED);
      m_rollerClaw.set(0.3);
      m_drivetrain.arcadeDrive(0, 0);
    }
    else if(timeElapsed < AUTO_DRIVE_DELAY_S + AUTO_DRIVE_TIME_S+1)
    {
      m_launchWheel.set(0);
      m_feedWheel.set(0);
      m_rollerClaw.set(0);
      m_drivetrain.arcadeDrive(-AUTO_DRIVE_SPEED, 0);
    }
    else if(timeElapsed < AUTO_DRIVE_DELAY_S + (AUTO_DRIVE_TIME_S)+2.56)
    {
      m_launchWheel.set(0);
      m_feedWheel.set(0);
      m_rollerClaw.set(0);
      m_drivetrain.arcadeDrive(-AUTO_DRIVE_SPEED, 0.8);
    }
    else if(timeElapsed < AUTO_DRIVE_DELAY_S + (AUTO_DRIVE_TIME_S)+6)
    {
      m_launchWheel.set(0);
      m_feedWheel.set(0);
      m_rollerClaw.set(0);
      m_drivetrain.arcadeDrive(-AUTO_DRIVE_SPEED, 0);
    }
    else
    {
      m_drivetrain.arcadeDrive(0, 0);
    }
  }
  if (m_autoSelected == kLaunchAndDriveAmp && ally.get()== Alliance.Blue){
    if(timeElapsed < AUTO_LAUNCH_DELAY_S)
    {
      m_launchWheel.set(LAUNCHER_SPEED);
      m_feedWheel.set(LAUNCHER_SPEED);
      m_rollerClaw.set(0);
      m_drivetrain.arcadeDrive(0, 0);

    }
    else if(timeElapsed < AUTO_DRIVE_DELAY_S + AUTO_DRIVE_TIME_S)
    {
      m_launchWheel.set(LAUNCHER_SPEED);
      m_feedWheel.set(LAUNCHER_SPEED);
      m_rollerClaw.set(0.3);
      m_drivetrain.arcadeDrive(0, 0);
    }
    else if(timeElapsed < AUTO_DRIVE_DELAY_S + (AUTO_DRIVE_TIME_S)+2.4)
    {
      m_launchWheel.set(0);
      m_feedWheel.set(0);
      m_rollerClaw.set(0);
      m_pivotMotor.set(-0.27);
      m_drivetrain.arcadeDrive(0, 0);
    }
    else if(timeElapsed < AUTO_DRIVE_DELAY_S + AUTO_DRIVE_TIME_S+3.4)
    {
      m_pivotMotor.set(0);
      m_launchWheel.set(0);
      m_feedWheel.set(0);
      m_rollerClaw.set(0);
      m_drivetrain.arcadeDrive(-AUTO_DRIVE_SPEED, 0);
    }
    else if(timeElapsed < AUTO_DRIVE_DELAY_S + (AUTO_DRIVE_TIME_S)+3.80)
    {
      m_pivotMotor.set(0);
      m_launchWheel.set(0);
      m_feedWheel.set(0);
      m_rollerClaw.set(0);
      m_drivetrain.arcadeDrive(0, -0.7);
    }
    else if(timeElapsed < AUTO_DRIVE_DELAY_S + (AUTO_DRIVE_TIME_S)+6)
    {
      m_pivotMotor.set(0);
      m_launchWheel.set(0);
      m_feedWheel.set(0);
      m_rollerClaw.set(-0.6);
      m_drivetrain.arcadeDrive(-AUTO_DRIVE_SPEED, 0);
    }
    else if(timeElapsed < AUTO_DRIVE_DELAY_S + (AUTO_DRIVE_TIME_S)+8.4)
    {
      m_pivotMotor.set(0.27);
      m_launchWheel.set(0);
      m_feedWheel.set(0);
      m_rollerClaw.set(-0.6);
      m_drivetrain.arcadeDrive(AUTO_DRIVE_SPEED, 0);
    }
    else if(timeElapsed < AUTO_DRIVE_DELAY_S + (AUTO_DRIVE_TIME_S)+8.8)
    {
      m_pivotMotor.set(0);
      m_launchWheel.set(0);
      m_feedWheel.set(0);
      m_rollerClaw.set(0);
      m_drivetrain.arcadeDrive(0, 0.7);
    }
    else if(timeElapsed < AUTO_DRIVE_DELAY_S + (AUTO_DRIVE_TIME_S)+9.6)
    {
      m_pivotMotor.set(0);
      m_launchWheel.set(LAUNCHER_SPEED);
      m_feedWheel.set(LAUNCHER_SPEED);
      m_rollerClaw.set(0);
      m_drivetrain.arcadeDrive(AUTO_DRIVE_SPEED, 0.7);
    }
    else if(timeElapsed < AUTO_DRIVE_DELAY_S + (AUTO_DRIVE_TIME_S)+10.6)
    {
      m_pivotMotor.set(0);
      m_launchWheel.set(LAUNCHER_SPEED);
      m_feedWheel.set(LAUNCHER_SPEED);
      m_rollerClaw.set(0.3);
      m_drivetrain.arcadeDrive(0, 0);
    }
    else if(timeElapsed < AUTO_DRIVE_DELAY_S + (AUTO_DRIVE_TIME_S)+14.6)
    {
      m_pivotMotor.set(0);
      m_launchWheel.set(0);
      m_feedWheel.set(0);
      m_rollerClaw.set(0);
      m_drivetrain.arcadeDrive(0, 0);
    }
    else
    {
      m_pivotMotor.set(0);
      m_launchWheel.set(0);
      m_feedWheel.set(0);
      m_rollerClaw.set(0);
      m_drivetrain.arcadeDrive(0, 0);
    }
  }
    if (m_autoSelected == kLaunch){
    if(timeElapsed < AUTO_LAUNCH_DELAY_S)
    {
      m_launchWheel.set(LAUNCHER_SPEED);
      m_feedWheel.set(LAUNCHER_SPEED);
      m_rollerClaw.set(0);
      m_drivetrain.arcadeDrive(0, 0);

    }
    else if(timeElapsed < AUTO_DRIVE_DELAY_S + AUTO_DRIVE_TIME_S)
    {
      m_launchWheel.set(LAUNCHER_SPEED);
      m_feedWheel.set(LAUNCHER_SPEED);
      m_rollerClaw.set(0.3);
      m_drivetrain.arcadeDrive(0, 0);
    }
    else if(timeElapsed < AUTO_DRIVE_DELAY_S + AUTO_DRIVE_TIME_S+1)
    {
      m_launchWheel.set(0);
      m_feedWheel.set(0);
      m_rollerClaw.set(0);
      m_drivetrain.arcadeDrive(0, 0);
    }

    else if(timeElapsed < AUTO_DRIVE_DELAY_S + (AUTO_DRIVE_TIME_S)+1.56)
    {
      m_launchWheel.set(0);
      m_feedWheel.set(0);
      m_rollerClaw.set(0);
      m_drivetrain.arcadeDrive(0, 0);
    }
    else if(timeElapsed < AUTO_DRIVE_DELAY_S + (AUTO_DRIVE_TIME_S)+6)
    {
      m_launchWheel.set(0);
      m_feedWheel.set(0);
      m_rollerClaw.set(0);
      m_drivetrain.arcadeDrive(-AUTO_DRIVE_SPEED, 0);
    }
    else
    {
      m_drivetrain.arcadeDrive(0, 0);
    }
  }
  if (m_autoSelected == kLaunchAndDrive){
    if(timeElapsed < AUTO_LAUNCH_DELAY_S)
    {
      m_launchWheel.set(LAUNCHER_SPEED);
      m_feedWheel.set(LAUNCHER_SPEED);
      m_rollerClaw.set(0);
      m_drivetrain.arcadeDrive(0, 0);

    }
    else if(timeElapsed < AUTO_DRIVE_DELAY_S + AUTO_DRIVE_TIME_S)
    {
      m_launchWheel.set(LAUNCHER_SPEED);
      m_feedWheel.set(LAUNCHER_SPEED);
      m_rollerClaw.set(0.3);
      m_drivetrain.arcadeDrive(0, 0);
    }
    else if(timeElapsed < AUTO_DRIVE_DELAY_S + AUTO_DRIVE_TIME_S+1)
    {
      m_launchWheel.set(0);
      m_feedWheel.set(0);
      m_rollerClaw.set(0);
      m_drivetrain.arcadeDrive(0, 0);
    }
    else if(timeElapsed < AUTO_DRIVE_DELAY_S + (AUTO_DRIVE_TIME_S)+1.56)
    {
      m_launchWheel.set(0);
      m_feedWheel.set(0);
      m_rollerClaw.set(0);
      m_drivetrain.arcadeDrive(-AUTO_DRIVE_SPEED, 0);
    }
    else if(timeElapsed < AUTO_DRIVE_DELAY_S + (AUTO_DRIVE_TIME_S)+6)
    {
      m_launchWheel.set(0);
      m_feedWheel.set(0);
      m_rollerClaw.set(0);
      m_drivetrain.arcadeDrive(-AUTO_DRIVE_SPEED, 0);
    }
    else
    {
      m_drivetrain.arcadeDrive(0, 0);
    }
  }
  if (m_autoSelected == kdoubleCenter){
    if(timeElapsed < AUTO_LAUNCH_DELAY_S)
    {
      m_launchWheel.set(LAUNCHER_SPEED);
      m_feedWheel.set(LAUNCHER_SPEED);
      m_rollerClaw.set(0);
      m_drivetrain.arcadeDrive(0, 0);

    }
    else if(timeElapsed < AUTO_DRIVE_DELAY_S + AUTO_DRIVE_TIME_S)
    {
      m_launchWheel.set(LAUNCHER_SPEED);
      m_feedWheel.set(LAUNCHER_SPEED);
      m_rollerClaw.set(0.3);
      m_drivetrain.arcadeDrive(0, 0);
    }
    else if(timeElapsed < AUTO_DRIVE_DELAY_S + AUTO_DRIVE_TIME_S+1)
    {
      m_launchWheel.set(0);
      m_feedWheel.set(0);
      m_rollerClaw.set(0);
      m_drivetrain.arcadeDrive(0, 0);
    }
    else if(timeElapsed < AUTO_DRIVE_DELAY_S + (AUTO_DRIVE_TIME_S)+3.4)
    {
      m_launchWheel.set(0);
      m_feedWheel.set(0);
      m_rollerClaw.set(0);
      m_pivotMotor.set(-0.27);
      m_drivetrain.arcadeDrive(0, 0);
    }
    else if(timeElapsed < AUTO_DRIVE_DELAY_S + (AUTO_DRIVE_TIME_S)+5.56)
    {
      m_launchWheel.set(0);
      m_feedWheel.set(0);
      m_rollerClaw.set(-0.4);
      m_pivotMotor.set(0);
      m_drivetrain.arcadeDrive(-AUTO_DRIVE_SPEED, 0);
    }
    else if(timeElapsed < AUTO_DRIVE_DELAY_S + (AUTO_DRIVE_TIME_S)+6)
    {
      m_launchWheel.set(0);
      m_feedWheel.set(0);
      m_rollerClaw.set(-0.4);
      m_pivotMotor.set(0);
      m_drivetrain.arcadeDrive(-AUTO_DRIVE_SPEED, 0);
    }
    else if(timeElapsed < AUTO_DRIVE_DELAY_S + (AUTO_DRIVE_TIME_S)+8.4)
    {
      m_launchWheel.set(0);
      m_feedWheel.set(0);
      m_rollerClaw.set(-0.4);
      m_pivotMotor.set(0.27);
      m_drivetrain.arcadeDrive(AUTO_DRIVE_SPEED, 0);
    }
    else if(timeElapsed < AUTO_DRIVE_DELAY_S + (AUTO_DRIVE_TIME_S)+9.8)
    {
      m_launchWheel.set(LAUNCHER_SPEED);
      m_feedWheel.set(LAUNCHER_SPEED);
      m_rollerClaw.set(0.3);
      m_pivotMotor.set(0);
      m_drivetrain.arcadeDrive(0, 0);
    }
    else if(timeElapsed < 15)
    {
      m_launchWheel.set(LAUNCHER_SPEED);
      m_feedWheel.set(LAUNCHER_SPEED);
      m_rollerClaw.set(0.3);
      m_pivotMotor.set(0);
      m_drivetrain.arcadeDrive(-AUTO_DRIVE_SPEED, 0);
    }
    else if (timeElapsed >= 15){
          m_launchWheel.set(0);
          m_feedWheel.set(0);
      m_rollerClaw.set(0);
      m_pivotMotor.set(0);

      m_drivetrain.arcadeDrive(0, 0);

    }
    else 
    {
    m_launchWheel.set(0);
      m_feedWheel.set(0);
      m_rollerClaw.set(0);
      m_pivotMotor.set(0);

      m_drivetrain.arcadeDrive(0, 0);
    }
  }
  
  

    /* For an explanation on differintial drive, squaredInputs, arcade drive and tank drive see the bottom of this file */
  }

  /** This function is called once when teleop is enabled. */
  @Override
  public void teleopInit() {
    /*
     * Motors can be set to idle in brake or coast mode.
     *
     * Brake mode effectively shorts the leads of the motor when not running, making it more
     * difficult to turn when not running.
     *
     * Coast doesn't apply any brake and allows the motor to spin down naturally with the robot's momentum.
     *
     * (touch the leads of a motor together and then spin the shaft with your fingers to feel the difference)
     *
     * This setting is driver preference. Try setting the idle modes below to kBrake to see the difference.
     */
    leftRear.setIdleMode(IdleMode.kCoast);
    leftFront.setIdleMode(IdleMode.kCoast);
    rightRear.setIdleMode(IdleMode.kCoast);
    rightFront.setIdleMode(IdleMode.kCoast);
  }
  private void rainbow() {
    // For every pixel
    for (var i = 0; i < m_ledBuffer.getLength(); i++) {
      // Calculate the hue - hue is easier for rainbows because the color
      // shape is a circle so only one value needs to precess
      final var hue = (m_rainbowFirstPixelHue + (i * 180 / m_ledBuffer.getLength())) % 180;
      // Set the value
      m_ledBuffer.setHSV(i, hue, 255, 128);
    }
    // Increase by to make the rainbow "move"
    m_rainbowFirstPixelHue += 3;
    // Check bounds
    m_rainbowFirstPixelHue %= 180;
  }
  public void setSoftLimits(){
        m_pivotMotor.restoreFactoryDefaults();
        //m_pivotMotor.enableSoftLimit(SoftLimitDirection.kForward, true);
        //m_pivotMotor.enableSoftLimit(SoftLimitDirection.kReverse, true);
        //m_pivotMotor.setSoftLimit(SoftLimitDirection.kForward, PIVOT_MAX);		// 15
        //m_pivotMotor.setSoftLimit(SoftLimitDirection.kReverse, PIVOT_MIN);		// 0
  }
  public boolean pivotReady(float target){
    return Math.abs((this.encoder.getAbsolutePosition() * 360) - target) <= 3;
  }

  /** This function is called periodically during operator control. */
  @Override
  public void teleopPeriodic() {
      
    
    /*
     * Spins up the launcher wheel
     */
    if (m_manipController.getRawButton(1)) {
      m_launchWheel.set(LAUNCHER_SPEED);
      m_feedWheel.set(FEEDER_OUT_SPEED);
      rainbow();
     
      m_led.setData(m_ledBuffer);
    }
    else if(m_manipController.getRawButtonReleased(1))
    {
      m_launchWheel.set(0);
      m_feedWheel.set(0);
    }

    /*
     * Spins feeder wheel, wait for launch wheel to spin up to full speed for best results
     */
    if (m_manipController.getRawButton(6))
    {
      m_pivotMotor.set(0.27);
    }
    else if(m_manipController.getRawButtonReleased(6))
    {
      m_pivotMotor.set(0);
    }

    /*
     * While the button is being held spin both motors to intake note
     */
    if(m_manipController.getRawButton(5))
    {
      m_pivotMotor.set(-0.27);
    }
    else if(m_manipController.getRawButtonReleased(5))
    {
      m_pivotMotor.set(0);
    }

    /*
     * While the amp button is being held, spin both motors to "spit" the note
     * out at a lower speed into the amp
     *
     * (this may take some driver practice to get working reliably)
     */
    if(m_manipController.getRawButton(2))
    {
      m_feedWheel.set(-FEEDER_AMP_SPEED);
      m_launchWheel.set(-LAUNCHER_AMP_SPEED);
      for (var i = 0; i < m_ledBuffer.getLength(); i++) {
        // Sets the specified LED to the HSV values for red
        m_ledBuffer.setRGB(i, 255, 0, 0);
     }
     
     m_led.setData(m_ledBuffer);
    }
    else if(m_manipController.getRawButtonReleased(2))
    {
      m_feedWheel.set(0);
      m_launchWheel.set(0);
    }
    if(m_manipController.getRawButton(7))
    {
      m_rollerClaw.set(0.6);
      for (var i = 0; i < m_ledBuffer.getLength(); i++) {
        // Sets the specified LED to the HSV values for red
        m_ledBuffer.setRGB(i, 255, 255, 0);
     }
     
     m_led.setData(m_ledBuffer);
    }
    else if(m_manipController.getRawButtonReleased(7))
    {
      m_rollerClaw.set(0);
    }

    if(m_driverController.getRawButton(5)){
      m_climber_1.set(CLIMER_OUTPUT_POWER);
      m_climber_2.set(-CLIMER_OUTPUT_POWER);
    }
    else if(m_driverController.getRawButtonReleased(5)){
      m_climber_1.set(0);
      m_climber_2.set(0);
    }
    if(m_driverController.getRawButton(6)){
      m_climber_1.set(-CLIMER_OUTPUT_POWER);
      m_climber_2.set(CLIMER_OUTPUT_POWER);
    }
    else if(m_driverController.getRawButtonReleased(6)){
      m_climber_1.set(0);
      m_climber_2.set(0);
    }
    if(m_driverController.getRawButton(3)){
      m_climber_1.set(-CLIMER_OUTPUT_POWER);
      m_climber_2.set(CLIMER_OUTPUT_POWER);
    }
    else if(m_driverController.getRawButtonReleased(3)){
      //m_climber_1.set(0);
      m_climber_2.set(0);
    }
    if(m_driverController.getRawButton(4)){
      //m_climber_1.set(-CLIMER_OUTPUT_POWER);
      m_climber_2.set(CLIMER_OUTPUT_POWER);
    }
    else if(m_driverController.getRawButtonReleased(3)){
      //m_climber_1.set(0);
      m_climber_2.set(0);
    }
    /**
     * Hold one of the two buttons to either intake or exjest note from roller claw
     * 
     * One button is positive claw power and the other is negative
     * 
     * It may be best to have the roller claw passively on throughout the match to 
     * better retain notes but we did not test this
     */ 
    if(m_manipController.getRawButton(3))
    {
      m_rollerClaw.set(0.6);
      m_drivetrain.arcadeDrive(0, 0);
      for (var i = 0; i < m_ledBuffer.getLength(); i++) {
        // Sets the specified LED to the HSV values for red
        m_ledBuffer.setRGB(i, 0, 255, 0);
     }
     m_led.setData(m_ledBuffer);
    }
    else if(m_manipController.getRawButton(4))
    {
      m_rollerClaw.set(-0.45);
      for (var i = 0; i < m_ledBuffer.getLength(); i++) {
        // Sets the specified LED to the HSV values for red
        m_ledBuffer.setRGB(i, 255, 0, 255);
     }

      m_led.setData(m_ledBuffer);
    }
    else
    {
      m_rollerClaw.set(0);
    }

    /**
     * POV is the D-PAD (directional pad) on your controller, 0 == UP and 180 == DOWN
     * 
     * After a match re-enable your robot and unspool the climb
     */
    
  
    /*
     * Negative signs are here because the values from the analog sticks are backwards
     * from what we want. Pushing the stick forward returns a negative when we want a
     * positive value sent to the wheels.
     *
     * If you want to change the joystick axis used, open the driver station, go to the
     * USB tab, and push the sticks determine their axis numbers
     *
     * This was setup with a logitech controller, note there is a switch on the back of the
     * controller that changes how it functions
     */
    m_drivetrain.arcadeDrive(-m_driverController.getRawAxis(1), -m_driverController.getRawAxis(4), false);
  }
}


/*
 * The kit of parts drivetrain is known as differential drive, tank drive or skid-steer drive.
 *
 * There are two common ways to control this drivetrain: Arcade and Tank
 *
 * Arcade allows one stick to be pressed forward/backwards to power both sides of the drivetrain to move straight forwards/backwards.
 * A second stick (or the second axis of the same stick) can be pushed left/right to turn the robot in place.
 * When one stick is pushed forward and the other is pushed to the side, the robot will power the drivetrain
 * such that it both moves fowards and turns, turning in an arch.
 *
 * Tank drive allows a single stick to control of a single side of the robot.
 * Push the left stick forward to power the left side of the drive train, causing the robot to spin around to the right.
 * Push the right stick to power the motors on the right side.
 * Push both at equal distances to drive forwards/backwards and use at different speeds to turn in different arcs.
 * Push both sticks in opposite directions to spin in place.
 *
 * arcardeDrive can be replaced with tankDrive like so:
 *
 * m_drivetrain.tankDrive(-m_driverController.getRawAxis(1), -m_driverController.getRawAxis(5))
 *
 * Inputs can be squared which decreases the sensitivity of small drive inputs.
 *
 * It literally just takes (your inputs * your inputs), so a 50% (0.5) input from the controller becomes (0.5 * 0.5) -> 0.25
 *
 * This is an option that can be passed into arcade or tank drive:
 * arcadeDrive(double xSpeed, double zRotation, boolean squareInputs)
 *
 *
 * For more information see:
 * https://docs.wpilib.org/en/stable/docs/software/hardware-apis/motors/wpi-drive-classes.html
 *
 * https://github.com/wpilibsuite/allwpilib/blob/main/wpilibj/src/main/java/edu/wpi/first/wpilibj/drive/DifferentialDrive.java
 *
 */
