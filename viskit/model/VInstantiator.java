package viskit.model;

import java.util.List;
import java.util.Vector;
import java.util.Iterator;

/**
 * OPNAV N81 - NPS World Class Modeling (WCM)  2004 Projects
 * MOVES Institute
 * Naval Postgraduate School, Monterey, CA
 * www.nps.edu
 * By:   Mike Bailey
 * Date: Jun 15, 2004
 * Time: 9:43:42 AM
 */

public abstract class VInstantiator
{
  private String type;
  public String getType()
  {
    return type;
  }
  public VInstantiator(String type)
  {
    this.type = type;
  }
  abstract public VInstantiator vcopy();
  abstract public boolean isValid();

  /***********************************************************************/
  public static class FreeF extends VInstantiator
  {
    private String value;

    public FreeF(String type, String value)
    {
      super(type);
      setValue(value);
    }
    public String getValue()            {return value;}
    public void   setValue(String value){this.value = value;}

    public String toString()
    {
      return value;
    }

    public VInstantiator vcopy()
    {
      return new VInstantiator.FreeF(getType(),getValue());
    }
    public boolean isValid()
    {
      String t = getType();
      String v = getValue();
      return t != null & v != null & t.length()>0 && v.length()>0;
    }
  }

  /***********************************************************************/
  public static class Constr extends VInstantiator
  {
    private List args;

    public Constr(String type, List args)
    {
      super(type);
      setArgs(args);
    }

    public List getArgs()         {return args;}
    public void setArgs(List args){this.args = args;}

    public String toString()
    {
      String rets = "new "+getType()+ "(";
      rets = rets + (args.size()>0?((VInstantiator)args.get(0)).getType()+",...":"");
      return rets+")";
    }
    public VInstantiator vcopy()
    {
      Vector lis = new Vector();
      for (Iterator itr = getArgs().iterator(); itr.hasNext();) {
        VInstantiator vi = (VInstantiator) itr.next();
        lis.add(vi.vcopy());
      }
      return new VInstantiator.Constr(getType(),lis);
    }
    public boolean isValid()
    {
      if(getType()==null || getType().length() <=0)
        return false;
      for (Iterator itr = getArgs().iterator(); itr.hasNext();) {
        VInstantiator v = (VInstantiator) itr.next();
        if(!v.isValid())
          return false;
      }
      return true;
    }
  }

  /***********************************************************************/
  public static class Array extends VInstantiator
  {
    private List instantiators; // array dimension == size()
    public Array(String typ, List inst)
    {
      super(typ);
      setInstantiators(inst);
    }
    public VInstantiator vcopy()
    {
      Vector lis = new Vector();
      for (Iterator itr = getInstantiators().iterator(); itr.hasNext();) {
        VInstantiator vi = (VInstantiator) itr.next();
        lis.add(vi.vcopy());
      }
      return new VInstantiator.Array(getType(),getInstantiators());
    }

    public List getInstantiators()
    {
      return instantiators;
    }

    public void setInstantiators(List instantiators)
    {
      this.instantiators = instantiators;
    }

    public String toString()
    {
      if(instantiators != null) {
        String t = getType().substring(0,getType().indexOf('['));
        return "new " + t + "[" + instantiators.size() + "]";
      }
      else
        return "";
    }
    public boolean isValid()
    {
      if(getType()==null || getType().length() <=0)
        return false;
      for (Iterator itr = getInstantiators().iterator(); itr.hasNext();) {
        VInstantiator v = (VInstantiator) itr.next();
        if(!v.isValid())
          return false;
      }
      return true;
    }
  }
  
  /***********************************************************************/
  public static class Factory extends VInstantiator
  {
    private String factoryClass;
    private String method;
    private List   params;

    public Factory(String type, String factoryClass, String method, List params)
    {
      super(type);
      setFactoryClass(factoryClass);
      setMethod(method);
      setParams(params);
    }

    public String getFactoryClass(){return factoryClass;}
    public String getMethod()      {return method;}
    public List   getParams()      {return params;}

    public void setFactoryClass(String s){this.factoryClass = s;}
    public void setMethod(String m)      {this.method = m;}
    public void setParams(List p)        {this.params = p;}

    public String toString()
    {
      return factoryClass+"."+method+"("+((VInstantiator)params.get(0)).getType()+",...)";
    }
    public VInstantiator vcopy()
    {
      Vector lis = new Vector();
      for (Iterator itr = lis.iterator(); itr.hasNext();) {
        VInstantiator vi = (VInstantiator) itr.next();
        lis.add(vi.vcopy());
      }
      return new VInstantiator.Factory(getType(),getFactoryClass(),getMethod(),lis);
    }
    public boolean isValid()
    {
      String t=getType(),fc=getFactoryClass(),m=getMethod();
      if ( t == null || fc == null || m == null ||
           t.length()<=0 || fc.length()<= 0 || m.length()<=0)
        return false;

      for (Iterator itr = getParams().iterator(); itr.hasNext();) {
        VInstantiator v = (VInstantiator) itr.next();
        if(!v.isValid())
          return false;
      }
      return true;
    }

  }
}