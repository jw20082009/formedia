package com.wantee.camera.request;

import java.util.Iterator;
import java.util.LinkedList;

public class RequestQueue<T extends RequestQueue.Data> {
    public interface IQueueListener<T> {
        void onAutoRemove(T data);
        void onDrop(T data);
    }
    public interface IOptionalChecker<T extends Data> {
        boolean onCheck(T data);
    }

    public static class Data{
        public int requestId = -1;
        public int dataTypeId = -1;
        public int excludeTypeId = -1; /** 在入队列之前，删除队列中已有的所有dataTypeId为excludeTypeId的值 */
        public int checkTypeId = -1; /** 在入队列之前，
                                  * 1. 如果有IOptionChecker，会使用Checker遍历队列中已有元素，检查如果有任意元素结果为true则当前数据可入队列，否则丢弃
                                  * 2. 如果没有IOptionChecker, 会遍历队列中是否存在dataTypeId为checkTypeId的值，否则丢弃 */
        public IOptionalChecker<? extends Data> checker;
        public CheckType checkType;

        public Data(CheckType type, int typeId) {
            this.checkType = type;
            this.dataTypeId = typeId;
        }

        public Data() {
            this(CheckType.Normal, 0);
        }

        public Data setCheckTypeId(int checkTypeId) {
            this.checkTypeId = checkTypeId;
            return this;
        }

        public <T extends Data> Data setOptionalChecker(IOptionalChecker<T> checker) {
            this.checker = checker;
            return this;
        }

        public void setRequestId(int requestId) {
            this.requestId = requestId;
        }
    }
    public enum CheckType {
        Normal, /** 数据直接入队 */
        Optional;
    }
    private final LinkedList<T> mData = new LinkedList<>();
    private IQueueListener<T> mListener;
    private int mRequestCode;
    public RequestQueue(IQueueListener<T> listener) {
        mListener = listener;
    }

    private void notifyAutoRemove(T data) {
        if (mListener != null) {
            mListener.onAutoRemove(data);
        }
    }

    private void notifyDrop(T data) {
        if (mListener != null) {
            mListener.onDrop(data);
        }
    }

    public int offer(T data) {
        synchronized (mData) {
            CheckType type = data.checkType;
            boolean canOffer = true;
            if (type == CheckType.Optional) {
                canOffer = handleOptional(data);
            }
            if (canOffer) {
                int request = mRequestCode++;
                data.setRequestId(request);
                mData.offerLast(data);
                return request;
            } else {
                notifyDrop(data);
                return -1;
            }
        }
    }

    public T peek() {
        synchronized (mData) {
            return mData.peekFirst();
        }
    }

    public T remove() {
        synchronized (mData) {
            return mData.removeFirst();
        }
    }

    /**
     * -1与任意值匹配
     */
    private boolean isTypeMatch(int typeId1, int typeId2) {
        if (typeId1 == -1 || typeId2 == -1) {
            return true;
        }
        return typeId1 == typeId2;
    }

    /**
     * 1. 遍历检查队列中已有元素，若匹配excludeTypeId则会将该元素移除
     * 2. 遍历检查队列中已有元素，若存在某元素满足IOptionChecker的条件，则本元素可以正常被添加到队列
     * @param data 当前需要被添加到队列的元素
     * @return 当前元素是否可以被添加到队列
     */
    private boolean handleOptional(T data) {
        Iterator<T> it = mData.iterator();
        boolean result = data.checker == null && data.checkTypeId < 0;
        while(it.hasNext()) {
            T d = it.next();
            if (isTypeMatch(data.excludeTypeId, d.dataTypeId)) {
                notifyAutoRemove(d);
                it.remove();
            }
            if (data.checker != null) {
                IOptionalChecker<T> checker = (IOptionalChecker<T>) data.checker;
                if (checker.onCheck(d)) {
                    result = true;
                }
            } else if (isTypeMatch(data.checkTypeId, d.dataTypeId)) {
                result = true;
            }
        }
        return result;
    }
}
