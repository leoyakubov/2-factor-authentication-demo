const React = require("react");

function Form({ children, onFinish, className, initialValues, ...props }) {
  const handleSubmit = (event) => {
    event.preventDefault();

    const values = {};

    Array.from(event.currentTarget.elements).forEach((element) => {
      if (!element.name || element.disabled) {
        return;
      }

      if (element.type === "checkbox") {
        values[element.name] = element.checked;
        return;
      }

      if (element.type === "radio") {
        if (element.checked) {
          values[element.name] = element.value;
        }
        return;
      }

      values[element.name] = element.value;
    });

    if (onFinish) {
      onFinish(values);
    }
  };

  return React.createElement(
    "form",
    {
      className,
      onSubmit: handleSubmit,
      ...props,
    },
    children
  );
}

function FormItem({ name, valuePropName, children, ...props }) {
  const childArray = React.Children.toArray(children);

  if (childArray.length !== 1 || !React.isValidElement(childArray[0])) {
    return React.createElement("div", props, children);
  }

  const child = childArray[0];

  const childProps = {};

  if (name) {
    childProps.name = name;
  }

  if (valuePropName === "checked" && child.props.type !== "checkbox") {
    childProps.type = "checkbox";
  }

  return React.createElement("div", props, React.cloneElement(child, childProps));
}

Form.Item = FormItem;

function Input(props) {
  return React.createElement("input", props);
}

function Button({
  children,
  htmlType,
  onClick,
  loading,
  disabled,
  block,
  shape,
  size,
  type,
  ...props
}) {
  return React.createElement(
    "button",
    {
      type: htmlType || type || "button",
      onClick,
      disabled: loading || disabled,
      ...props,
    },
    children
  );
}

function Checkbox({ children, checked, onChange, ...props }) {
  return React.createElement(
    "label",
    null,
    React.createElement("input", {
      type: "checkbox",
      checked,
      onChange,
      ...props,
    }),
    children
  );
}

function Alert({ message, description, type }) {
  return React.createElement(
    "div",
    {
      role: "alert",
      "data-type": type,
    },
    React.createElement("strong", null, message),
    description ? React.createElement("div", null, description) : null
  );
}

function Avatar({ src, children, icon, ...props }) {
  return React.createElement(
    "div",
    props,
    src ? React.createElement("img", { src, alt: "avatar" }) : children || icon
  );
}

function Card({ children, actions, ...props }) {
  return React.createElement(
    "section",
    props,
    children,
    actions ? React.createElement("div", null, actions) : null
  );
}

Card.Meta = function Meta({ avatar, title, description }) {
  return React.createElement(
    "div",
    null,
    avatar,
    title ? React.createElement("div", null, title) : null,
    description ? React.createElement("div", null, description) : null
  );
};

const Typography = {
  Title: ({ children, level = 1 }) =>
    React.createElement(`h${level}`, null, children),
};

const notification = {
  error: jest.fn(),
};

module.exports = {
  Alert,
  Avatar,
  Button,
  Card,
  Checkbox,
  Form,
  Input,
  Typography,
  notification,
};
