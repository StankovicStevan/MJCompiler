// generated with ast extension for cup
// version 0.8
// 29/5/2021 2:52:51


package rs.ac.bg.etf.pp1.ast;

public class VarDeclListHelpStmt extends VarDeclListHelp {

    private VarDeclListHelp VarDeclListHelp;
    private VarDecl VarDecl;

    public VarDeclListHelpStmt (VarDeclListHelp VarDeclListHelp, VarDecl VarDecl) {
        this.VarDeclListHelp=VarDeclListHelp;
        if(VarDeclListHelp!=null) VarDeclListHelp.setParent(this);
        this.VarDecl=VarDecl;
        if(VarDecl!=null) VarDecl.setParent(this);
    }

    public VarDeclListHelp getVarDeclListHelp() {
        return VarDeclListHelp;
    }

    public void setVarDeclListHelp(VarDeclListHelp VarDeclListHelp) {
        this.VarDeclListHelp=VarDeclListHelp;
    }

    public VarDecl getVarDecl() {
        return VarDecl;
    }

    public void setVarDecl(VarDecl VarDecl) {
        this.VarDecl=VarDecl;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(VarDeclListHelp!=null) VarDeclListHelp.accept(visitor);
        if(VarDecl!=null) VarDecl.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(VarDeclListHelp!=null) VarDeclListHelp.traverseTopDown(visitor);
        if(VarDecl!=null) VarDecl.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(VarDeclListHelp!=null) VarDeclListHelp.traverseBottomUp(visitor);
        if(VarDecl!=null) VarDecl.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("VarDeclListHelpStmt(\n");

        if(VarDeclListHelp!=null)
            buffer.append(VarDeclListHelp.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(VarDecl!=null)
            buffer.append(VarDecl.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [VarDeclListHelpStmt]");
        return buffer.toString();
    }
}
