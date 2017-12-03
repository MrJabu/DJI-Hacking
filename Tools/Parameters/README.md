## Video

https://www.youtube.com/watch?v=H2d784lgtuA&feature=youtu.be

---

# Ability to modify device parameters


This can be done on any version you use.

In the folder you created go to \DJI Assistant 2\AppFiles

With Notepad++, edit the main.js file. (Notepad++ here: https://notepad-plus-plus.org - just get it. Great program for editing.

Look for " // mainWindow.webContents.openDevTools()"

Delete the "// " (slash slash from that line)

Save the file

Open DJI Assistant 2

You will need to locate the Resource tab on the right

Locate the "Debug" field and change the value from '0' to '1'

---

### Change descend rate in GPS mode for faster descend rate, but not as fast as Sport+
normal_cfg_vel_down -3(stock) -6
normal_cfg_acc_down -5(stock) -6

### Change the High Wind Warnings so they don't trigger so often (from rford71)
##### stock
g_config_air_est_big_wind_level1 6

g_config_air_est_big_wind_level2 9
##### my adjustments
g_config_air_est_big_wind_level1 10

g_config_air_est_big_wind_level2 13
##### Kilrah recommends:
g_config_air_est_big_wind_level1 8

g_config_air_est_big_wind_level2 11

### Here are some parameter settings you can change

### Recommended settings from digdat0 and others. These are the settings I use for the Mavic:
##### Sport+
g_config_mode_sport_cfg_tilt_atti_range = 43 (35 stock)

g_config_mode_sport_cfg_vert_vel_up= 9

g_config_mode_sport_cfg_vert_vel_down = -10

g_config_mode_sport_cfg_vert_acc_up = 9

g_config_mode_sport_cfg_vert_acc_down = -10

g_config_fw_cfg_max_speed = 20

brake_sensitive_gain = 110

g_config_mode_mode_sport_cfg_rc_scale = 1

g_config_control_dyn_tilt_min = 28

##### GPS+
g_config_mode_normal_cfg_tilt_atti_range = 25

g_config_mode_normal_cfg_vert_vel_up = 8

g_config_mode_normal_cfg_vert_vel_down = -6

g_config_mode_normal_cfg_vert_acc_up = 8

g_config_mode_normal_cfg_vert_acc_down = -6

##### Height+*remove height restrictions*
g_config_flying_limit_height_limit_enabled = 2

g_config_flying_limit_limit_height_abs_without_gps = 3000

g_config_flying_limit_limit_height_abs = 3000

g_config_flying_limit_limit_height_rel = 3000

##### NFZ Changes *700 and below*
g_config_airport_limit_cfg_cfg_disable_airport_fly _limit = 1

g_config_airport_limit_cfg_cfg_limit_data = 20250910

##### Wind-*remove wind warnings*
g_config_air_est_big_wind_level1 = 8

g_config_air_est_big_wind_level2 = 11

##### RTH Speed increase
g_config_go_home_gohome_idle_vel = 13

##### Turn off Auto Land on low battery *you will get warnings, but will not auto land if the -value is 0*
g_config_landing_auto_landing_vel_L1 = -1 (-1) *L1 sets descend rate below approx. 20m

g_config_landing_auto_landing_vel_L2 = -6 (-3) *L2 sets descend rate above 20m *fast descend rate for 1st stage of Auto landing*

*Be careful with this setting. If you have the value as 0, the quad will just sit there and you need to manually bring it down.*

##### Atti Mode (via sport mode button)*this will stick on .700, but will revert to GPS mode on newer firmware*
g_config_control_mode[1] (fswitch_selection_1) 8 = Sport Mode Default

g_config_control_mode[2] (fswitch_selection_2) 7 = GPS Mode Default

Changing either to 6 will add beginner mode on the switch

Changing either to 3 is ATTI so you can either change GPS mode or Sport Mode position to ATTI

### Setting that will level your gimbal when you land automatically
g_config_landing_ctrl_gimbal_pitch_to_horiz_enable 1 (0)

### Here are my Spark settings I fly with
##### Sport+
g_config_mode_sport_cfg_tilt_atti_range = 45 (35 stock)

g_config_mode_sport_cfg_vert_vel_up= 10

g_config_mode_sport_cfg_vert_vel_down = -6

g_config_mode_sport_cfg_vert_acc_up =10

g_config_mode_sport_cfg_vert_acc_down = -6

g_config_fw_cfg_max_speed = 20

brake_sensitive_gain = 150

##### GPS+
g_config_mode_normal_cfg_tilt_atti = 40

g_config_mode_normal_cfg_vert_vel_up = 8

g_config_mode_normal_cfg_vert_vel_down = -6

g_config_mode_normal_cfg_vert_acc_up = 8

g_config_mode_normal_cfg_vert_acc_down = -6

##### Height+*remove height restrictions*
g_config_flying_limit_height_limit_enabled = 2

g_config_flying_limit_limit_height_abs_without_gps = 3000

g_config_flying_limit_limit_height_abs = 3000

g_config_flying_limit_limit_height_rel = 3000

##### NFZ Changes *700 and below*
g_config_airport_limit_cfg_cfg_disable_airport_fly _limit = 1

g_config_airport_limit_cfg_cfg_limit_data = 20250910

##### Wind-*remove wind warnings*
g_config_air_est_big_wind_level1 = 8

g_config_air_est_big_wind_level2 = 11

##### RTH Speed increase
g_config_go_home_gohome_idle_vel = 13

##### Turn off Auto Land on low battery *you will get warnings, but will not auto land if the value is 0*
g_config_landing_auto_landing_vel_L1 = -1 (-1) *L1 sets descend rate below approx. 20m

g_config_landing_auto_landing_vel_L2 = -6 (-3) *L2 sets descend rate above 20m *fast descend rate for 1st stage of Auto landing*

##### Atti Mode (via sport mode button)*this will stick on .700, but will revert to GPS mode on newer firmware*
g_config_control_mode[1] (fswitch_selection_1) 8 = Sport Mode Default

g_config_control_mode[2] (fswitch_selection_2) 7 = GPS Mode Default

Changing either to 6 will add beginner mode on the switch

Changing either to 3 is ATTI so you can either change GPS mode or Sport Mode position to ATTI

### Setting that will level your gimbal when you land automatically
g_config_landing_ctrl_gimbal_pitch_to_horiz_enable 1 (1)
