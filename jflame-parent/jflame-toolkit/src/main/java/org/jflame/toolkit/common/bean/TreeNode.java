package org.jflame.toolkit.common.bean;

import java.beans.Transient;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * 树节点对象
 * 
 * @author yucan.zhang
 * @param <T> 属性对象类型
 */
public class TreeNode<T> implements Serializable {

    private static final long serialVersionUID = 1L;
    private String nodeName;
    private TreeNode<T> parent;
    private T attribute;//属性对象
    private final List<TreeNode<T>> children = new ArrayList<>();

    /**
     * 增加一个子节点
     * 
     * @param child 子节点
     */
    public void addNode(TreeNode<T> child) {
        children.add(child);
        child.setParent(this);
    }

    /**
     * 增加多个子节点
     * 
     * @param childs 子节点集合
     */
    public void addNodes(Collection<? extends TreeNode<T>> childs) {
        children.addAll(children);
        for (TreeNode<T> treeNode : childs) {
            treeNode.setParent(this);
        }
    }

    /**
     * 删除指定索引位置处的子节点
     * 
     * @param index
     * @return 返回删除的子节点,如果索引位置不存在返回null
     */
    public TreeNode<T> removeNode(int index) {
        if (index < 0 || index >= children.size()) {
            return null;
        }
        TreeNode<T> removeNode = children.remove(index);
        removeNode.setParent(null);
        return removeNode;
    }

    /**
     * 删除子节点
     * 
     * @param node 子节点
     * @return 如存在相等子节点删除并返回true,否则返回false
     */
    public boolean removeNode(TreeNode<T> node) {
        if (children.remove(node)) {
            node.setParent(null);
            return true;
        }
        return false;
    }

    /**
     * 返回所有子节点的迭代器
     * 
     * @return 子节点的迭代器
     */
    public Iterator<TreeNode<T>> nodeIterator() {
        return children.iterator();
    }

    /**
     * 返回 所有子节点的不可变列表
     * 
     * @return
     */
    public List<TreeNode<T>> getChildren() {
        return Collections.unmodifiableList(children);
    }

    /**
     * 返回直接父节点
     * 
     * @return
     */
    @Transient
    public TreeNode<T> getParent() {
        return parent;
    }

    /**
     * 是否叶子节点,即是否有子节点
     * 
     * @return
     */
    public boolean isLeaf() {
        return children.isEmpty();
    }

    /**
     * 返回 子节点的个数
     * 
     * @return
     */
    public int nodeSize() {
        return children.size();
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    protected void setParent(TreeNode<T> parent) {
        this.parent = parent;
    }

    public T getAttribute() {
        return attribute;
    }

    public void setAttribute(T attribute) {
        this.attribute = attribute;
    }

}
