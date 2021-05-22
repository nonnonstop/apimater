/**
 * Callback of creating preference at MainActivity
 * 
 * This function is called from MainActivity on UI thread.
 */
// eslint-disable-next-line @typescript-eslint/no-unused-vars
function onCreateDatPreference(fragment: Fragment, preference: Preference): ActivityResultLauncher {
    const preflauncher = Packages.com.nonnonstop.apimate.preflaunch.BuiltinPrefLauncher();
    return preflauncher.onCreateDatPreference(fragment);
}

/**
 * Callback of clicking preference at MainActivity
 * 
 * This function is called from MainActivity on UI thread.
 */
// eslint-disable-next-line @typescript-eslint/no-unused-vars
function onClickDatPreference(fragment: Fragment, launcher: ActivityResultLauncher): void {
    const preflauncher = Packages.com.nonnonstop.apimate.preflaunch.BuiltinPrefLauncher();
    return preflauncher.onClickDatPreference(
        launcher,
        'com.android.externalstorage.documents',
        'primary:Android/data/jp.co.airfront.android.a2chMate/files/2chMate/dat');
}
