// generated with ast extension for cup
// version 0.8
// 29/5/2021 2:52:51


package rs.ac.bg.etf.pp1.ast;

public class ClassDeclList implements SyntaxNode {

    private SyntaxNode parent;
    private int line;
    private String I1;
    private ExtendsType ExtendsType;
    private VarDeclListHelp VarDeclListHelp;
    private MethodDeclListBracket MethodDeclListBracket;

    public ClassDeclList (String I1, ExtendsType ExtendsType, VarDeclListHelp VarDeclListHelp, MethodDeclListBracket MethodDeclListBracket) {
        this.I1=I1;
        this.ExtendsType=ExtendsType;
        if(ExtendsType!=null) ExtendsType.setParent(this);
        this.VarDeclListHelp=VarDeclListHelp;
        if(VarDeclListHelp!=null) VarDeclListHelp.setParent(this);
        this.MethodDeclListBracket=MethodDeclListBracket;
        if(MethodDeclListBracket!=null) MethodDeclListBracket.setParent(this);
    }

    public String getI1() {
        return I1;
    }

    public void setI1(String I1) {
        this.I1=I1;
    }

    public ExtendsType getExtendsType() {
        return ExtendsType;
    }

    public void setExtendsType(ExtendsType ExtendsType) {
        this.ExtendsType=ExtendsType;
    }

    public VarDeclListHelp getVarDeclListHelp() {
        return VarDeclListHelp;
    }

    public void setVarDeclListHelp(VarDeclListHelp VarDeclListHelp) {
        this.VarDeclListHelp=VarDeclListHelp;
    }

    public MethodDeclListBracket getMethodDeclListBracket() {
        return MethodDeclListBracket;
    }

    public void setMethodDeclListBracket(MethodDeclListBracket MethodDeclListBracket) {
        this.MethodDeclListBracket=MethodDeclListBracket;
    }

    public SyntaxNode getParent() {
        return parent;
    }

    public void setParent(SyntaxNode parent) {
        this.parent=parent;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line=line;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(ExtendsType!=null) ExtendsType.accept(visitor);
        if(VarDeclListHelp!=null) VarDeclListHelp.accept(visitor);
        if(MethodDeclListBracket!=null) MethodDeclListBracket.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(ExtendsType!=null) ExtendsType.traverseTopDown(visitor);
        if(VarDeclListHelp!=null) VarDeclListHelp.traverseTopDown(visitor);
        if(MethodDeclListBracket!=null) MethodDeclListBracket.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(ExtendsType!=null) ExtendsType.traverseBottomUp(visitor);
        if(VarDeclListHelp!=null) VarDeclListHelp.traverseBottomUp(visitor);
        if(MethodDeclListBracket!=null) MethodDeclListBracket.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("ClassDeclList(\n");

        buffer.append(" "+tab+I1);
        buffer.append("\n");

        if(ExtendsType!=null)
            buffer.append(ExtendsType.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(VarDeclListHelp!=null)
            buffer.append(VarDeclListHelp.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(MethodDeclListBracket!=null)
            buffer.append(MethodDeclListBracket.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [ClassDeclList]");
        return buffer.toString();
    }
}
