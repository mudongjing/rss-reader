import Vue from 'vue';
export const createModal = (Component, props) => {
    const vm = new Vue({
        render: (h) =>
            h(Component, {
                props,
            }),
    }).$mount();
    document.body.appendChild(vm.$el);
    const ele = vm.$children[0];
    ele.destroy = function() {
        vm.$el.remove();
        ele.$destroy();
        vm.$destroy();
    };
    return ele;
};
