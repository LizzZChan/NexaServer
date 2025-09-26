import argparse
from langchain_openai import ChatOpenAI
from langchain_core.prompts import ChatPromptTemplate
from langchain_core.output_parsers import StrOutputParser
from .ui.ui_tree import UITree, UINodeBounds
from .utils.file_utils import load_string

def query_llm(ui_input: str):
    llm = ChatOpenAI()

    prompt = ChatPromptTemplate.from_messages([
        ("system", "{system}"),
        ("user", "{query}")
    ])

    output_parser = StrOutputParser()

    chain = prompt | llm | output_parser

    system = f' \
        你是一个顶级咨询师，会为用户的询问给出最直接的回答，如果用户需要选择，请回答几个你认为合适的选项，尽可能具体，不要回答大的类别。\
        接下来的每个输入都对应用户在手机界面上选择出的文本，需要分情况给出结果：\
        1. 如果输入大部分是外语，请翻译成中文。 \
        2. 如果是个提问句，给出明确的答案。 \
        3. 否则，给出对文本的解释。 \
        你总是能给出一个确定的答案。 \
    '

    query = f' \
        {ui_input} \
    '

    output = chain.invoke({"system": system, "query": query})
    return output

def search(ui_tree_str: str, gesture: list[float]) -> str:
    query_bounds = UINodeBounds.from_points(gesture)
    ui_tree = UITree(tree=ui_tree_str)
    ui_text = ui_tree.extract_text(query_bounds)
    if len(ui_text) == 0:
        return 'Empty'
    ui_input = '[' + ','.join(ui_text) + ']'
    result = query_llm(ui_input)
    return result

def main():
    parser = argparse.ArgumentParser(description='')

    parser.add_argument('--uitree', required=True, type=str)
    parser.add_argument('--gesture', required=True, type=str)
    
    args = parser.parse_args()

    ui_tree = load_string(args.uitree)
    gesture = list(map(float, load_string(args.gesture).strip().split(',')))
    search(ui_tree, gesture)

if __name__ == '__main__':
    main()