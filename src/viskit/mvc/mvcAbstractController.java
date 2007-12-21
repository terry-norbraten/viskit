package viskit.mvc;

/**
 * OPNAV N81 - NPS World Class Modeling (WCM) 2004 Projects
 * MOVES Institute
 * Naval Postgraduate School, Monterey CA
 * www.nps.edu
 * @author Mike Bailey
 * @since Mar 2, 2004
 * @since 11:30:36 AM
 * @version $Id: mvcAbstractController.java 1666 2007-12-17 05:24:41Z tdnorbra $
 *
 * From an article at www.jaydeetechnology.co.uk
 */
public abstract class mvcAbstractController implements mvcController
{
  private mvcView view;
  private mvcModel model;

  public mvcModel getModel()
  {
    return model;
  }

  public mvcView getView()
  {
    return view;
  }

  public void setModel(mvcModel model)
  {
    this.model = model;
  }

  public void setView(mvcView view)
  {
    this.view = view;
  }
}
