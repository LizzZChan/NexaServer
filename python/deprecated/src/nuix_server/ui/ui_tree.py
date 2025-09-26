from __future__ import annotations

import json
from pathlib import Path
from ..utils.file_utils import load_json

class UINodeBounds():
    MAX_VALUE = 1000000

    def __init__(
        self,
        top: int,
        bottom: int,
        left: int,
        right: int,
    ) -> None:
        self.top = top
        self.bottom = bottom
        self.left = left
        self.right = right

    def from_points(points: list[float]) -> UINodeBounds:
        top = UINodeBounds.MAX_VALUE
        bottom = -UINodeBounds.MAX_VALUE
        left = UINodeBounds.MAX_VALUE
        right = -UINodeBounds.MAX_VALUE
        for i in range(0, len(points), 3):
            x = points[i + 1]
            y = points[i + 2]
            top = min(top, y)
            bottom = max(bottom, y)
            left = min(left, x)
            right = max(right, x)
        return UINodeBounds(
            top=top,
            bottom=bottom,
            left=left,
            right=right
        )

    def inside(self, bound: UINodeBounds) -> bool:
        return self.top >= bound.top and \
               self.bottom <= bound.bottom and \
               self.left >= bound.left and \
               self.right <= bound.right
    
    def contain(self, bound: UINodeBounds) -> bool:
        return bound.inside(self)

    def intersect(self, bound: UINodeBounds) -> bool:
        return max(self.top, bound.top) <= min(self.bottom, bound.bottom) and \
               max(self.left, bound.left) <= min(self.right, bound.right)

    def __repr__(self):
        return f"[{self.top}, {self.bottom}, {self.left}, {self.right}]"
            
class UINode():
    def __init__(
        self,
        accessibility_focused: bool,
        bounds: UINodeBounds,
        checkable: bool,
        checked: bool,
        children: list[UINode],
        class_name: str,
        clickable: bool,
        descendant_count: int,
        dismissable: bool,
        editable: bool,
        enabled: bool,
        focusable: bool,
        focused: bool,
        invisible_to_user: bool,
        level: int,
        long_clickable: bool,
        package_name: str,
        password: bool,
        scrollable: bool,
        selected: bool,
        text: str
    ) -> None:
        self.accessibility_focused = accessibility_focused
        self.bounds = bounds
        self.checkable = checkable
        self.checked = checked
        self.children = children
        self.class_name = class_name
        self.clickable = clickable
        self.descendant_count = descendant_count
        self.dismissable = dismissable
        self.editable = editable
        self.enabled = enabled
        self.focusable = focusable
        self.focused = focused
        self.invisible_to_user = invisible_to_user
        self.level = level
        self.long_clickable = long_clickable
        self.package_name = package_name
        self.password = password
        self.scrollable = scrollable
        self.selected = selected
        self.text = text

    def traverse(self) -> list[UINode]:
        nodes = [self]
        if self.children is not None:
            for child in self.children:
                nodes.extend(child.traverse())
        return nodes

class UITree():
    def __init__(self, tree: str = None, file: Path = None):
        self.tree = json.loads(tree) if file is None else load_json(file)
        self.root = self.load_from_dict(self.tree)

    def load_from_dict(self, tree: dict) -> UINode:
        bounds = UINodeBounds(
            top = tree['bounds']['top'],
            bottom = tree['bounds']['bottom'],
            left = tree['bounds']['left'],
            right = tree['bounds']['right'],
        ) if tree.get('bounds') is not None else None
        children = list(map(
            lambda x: self.load_from_dict(x),
            tree.get('children')
        )) if tree.get('children') is not None else None
        return UINode(
            accessibility_focused=tree.get('accessibilityFocused'),
            bounds=bounds,
            checkable=tree.get('checkable'),
            checked=tree.get('checked'),
            children=children,
            class_name=tree.get('className'),
            clickable=tree.get('clickable'),
            descendant_count=tree.get('descendantCount'),
            dismissable=tree.get('dismissable'),
            editable=tree.get('editable'),
            enabled=tree.get('enabled'),
            focusable=tree.get('focusable'),
            focused=tree.get('focused'),
            invisible_to_user=tree.get('invisibleToUser'),
            level=tree.get('level'),
            long_clickable=tree.get('longClickable'),
            package_name=tree.get('packageName'),
            password=tree.get('password'),
            scrollable=tree.get('scrollable'),
            selected=tree.get('selected'),
            text=tree.get('text'),
        )

    def extract_text(self, bounds: UINodeBounds) -> list[str]:
        result = []
        for node in self.root.traverse():
            if node.text is not None:
                if node.bounds.intersect(bounds):
                    result.append(node.text)
        return result