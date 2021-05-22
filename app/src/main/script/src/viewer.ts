/**
 * Open viewer
 * 
 * This function is called from LoadActivity on UI thread.
 */
// eslint-disable-next-line @typescript-eslint/no-unused-vars
function view(activity: Activity, htmlUrl: string): void {
    const viewer = Packages.com.nonnonstop.apimate.viewer.BuiltinViewer();
    viewer.view(activity);
}
