/**
 * Open viewer
 * 
 * This function is called from LoadActivity on UI thread.
 */
// eslint-disable-next-line @typescript-eslint/no-unused-vars
function view(activity: Activity, htmlUrl: string): void {
    const viewer = Packages.com.nonnonstop.apimate.viewer.BuiltinViewer600();
    viewer.view(
        activity,
        htmlUrl,
        "https://mi.5ch.net/test/read.cgi/news4vip/9245000000/",
        1000,
        true);
}
