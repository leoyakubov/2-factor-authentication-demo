const React = require("react");
const FormErrorsContext = React.createContext({});

function createForm() {
  let fieldErrors = {};
  const listeners = new Set();

  const notify = () => {
    listeners.forEach((listener) => listener({ ...fieldErrors }));
  };

  return {
    setFields(fields = []) {
      if (!Array.isArray(fields)) {
        return;
      }

      if (fields.length === 0) {
        fieldErrors = {};
        notify();
        return;
      }

      fields.forEach(({ name, errors }) => {
        if (!name) {
          return;
        }

        const nextErrors = Array.isArray(errors) ? errors.filter(Boolean) : [];
        if (nextErrors.length > 0) {
          fieldErrors[name] = nextErrors;
        } else {
          delete fieldErrors[name];
        }
      });

      notify();
    },
    resetFields() {
      fieldErrors = {};
      notify();
    },
    __subscribe(listener) {
      listeners.add(listener);
      listener({ ...fieldErrors });
      return () => listeners.delete(listener);
    },
  };
}

function Form({ children, onFinish, className, ...props }) {
  delete props.initialValues;
  const [formState] = React.useState(() => props.form || createForm());
  const [fieldErrors, setFieldErrors] = React.useState({});

  React.useEffect(() => {
    return formState.__subscribe(setFieldErrors);
  }, [formState]);

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
    FormErrorsContext.Provider,
    { value: fieldErrors },
    React.createElement(
      "form",
      {
        className,
        onSubmit: handleSubmit,
        ...props,
      },
      children
    )
  );
}

Form.useForm = function useForm() {
  const form = React.useMemo(() => createForm(), []);
  return [form];
};

function FormItem({ name, valuePropName, children, ...props }) {
  const fieldErrors = React.useContext(FormErrorsContext);
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

  const errorMessages = name ? fieldErrors[name] || [] : [];

  return React.createElement(
    "div",
    props,
    React.cloneElement(child, childProps),
    errorMessages.length > 0
      ? React.createElement(
          "div",
          { role: "alert", "data-type": "error" },
          errorMessages.map((message) =>
            React.createElement("div", { key: message }, message)
          )
        )
      : null
  );
}

Form.Item = FormItem;

function Input(props) {
  return React.createElement("input", props);
}

function Button({ children, htmlType, onClick, loading, disabled, type, ...props }) {
  delete props.block;
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
