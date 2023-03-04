2008 JUL 20

There are two differences between the trunk version of Viskit and the OA3302
branch.  Namely, the OA3302 branch is a simplified version of the trunk.  The
differences are in three source files and are annotated like this:

/* DIFF between OA3302 branch and trunk */
code that is commented out for OA3302 branch
/* End DIFF between OA3302 branch and trunk */

The affected source files are:
viskit.EventGraphAssemblyComboMainFrame
viskit.InternalAssemblyRunner
viskit.RunnerPanel2

A search on the above comments will expose where in the source these are.

2014 MAY 12

Cool vid on the anatomy of an Arrival Process: http://youtu.be/7zqFlCnrmbE

2023-03-04

If the ssh-agent drops identities, have to put them back for command line git to
work w/ ssh keys. Test by:

> ssh-add -L

If no keys are loaded, then:

> eval "$(ssh-agent -s)"
> ssh-add ~/.ssh/id_github_rsa

Remember to:
> git lfs push/pull

if any of the binaries have changed when in LFS mode
